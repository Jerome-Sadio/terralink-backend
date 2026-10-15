package sn.terralink.service

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.util.UUID

@Service
class MinioService(
    @Value("\${terralink.s3.endpoint}") private val endpoint: String,
    @Value("\${terralink.s3.access-key}") private val accessKey: String,
    @Value("\${terralink.s3.secret-key}") private val secretKey: String,
    @Value("\${terralink.s3.bucket-name}") private val bucket: String
) {
    private val logger = LoggerFactory.getLogger(MinioService::class.java)

    private val minioClient: MinioClient by lazy {
        MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()
    }

    private var bucketInitialized = false

    @Synchronized
    fun ensureBucketExists() {
        if (bucketInitialized) return
        try {
            val exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
                logger.info("MinIO Bucket '$bucket' created successfully.")
            }
            bucketInitialized = true
        } catch (e: Exception) {
            logger.warn("Could not verify or create MinIO bucket '$bucket': ${e.message}")
        }
    }

    fun uploadPhoto(file: MultipartFile, folder: String = "inspections"): UploadResult {
        ensureBucketExists()
        val originalName = file.originalFilename ?: "photo.jpg"
        val extension = originalName.substringAfterLast('.', "jpg")
        val uniqueName = "${UUID.randomUUID()}.$extension"
        val objectPath = "$folder/$uniqueName"
        val bytes = file.bytes

        // Calculate SHA-256 hash of image bytes
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        val sha256Hex = hashBytes.joinToString("") { "%02x".format(it) }

        val contentType = file.contentType ?: "image/jpeg"

        var photoUrl = "$endpoint/$bucket/$objectPath"
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(objectPath)
                    .stream(ByteArrayInputStream(bytes), bytes.size.toLong(), -1)
                    .contentType(contentType)
                    .build()
            )
            logger.info("Uploaded photo to MinIO: $photoUrl (SHA-256: $sha256Hex)")
        } catch (e: Exception) {
            logger.warn("MinIO upload unavailable (${e.message}), saving to local file storage...")
            val dir = java.io.File("uploads/$folder")
            if (!dir.exists()) dir.mkdirs()
            val localFile = java.io.File(dir, uniqueName)
            localFile.writeBytes(bytes)
            photoUrl = "/uploads/$folder/$uniqueName"
            logger.info("Saved photo to local storage: $photoUrl")
        }

        return UploadResult(
            photoUrl = photoUrl,
            metadataHash = sha256Hex,
            objectPath = objectPath,
            sizeBytes = bytes.size.toLong()
        )
    }
}

data class UploadResult(
    val photoUrl: String,
    val metadataHash: String,
    val objectPath: String,
    val sizeBytes: Long
)
