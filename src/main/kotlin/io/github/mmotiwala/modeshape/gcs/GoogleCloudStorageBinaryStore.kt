package io.github.mmotiwala.modeshape.gcs

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.StorageOptions
import org.modeshape.jcr.value.BinaryKey
import org.modeshape.jcr.value.BinaryValue
import org.modeshape.jcr.value.binary.AbstractBinaryStore
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.concurrent.TimeUnit
import com.google.cloud.storage.BlobInfo
import java.nio.charset.StandardCharsets.UTF_8
import org.modeshape.jcr.value.binary.FileSystemBinaryStore
import org.modeshape.jcr.value.binary.StoredBinaryValue
import org.modeshape.jcr.value.binary.TransientBinaryStore
import java.io.ByteArrayInputStream
import java.nio.channels.Channels


class GoogleCloudStorageBinaryStore() : AbstractBinaryStore() {

    companion object {
        private val log = LoggerFactory.getLogger(GoogleCloudStorageBinaryStore::class.java)
        const val ExtractedTextSuffix = "MODESHAPE-TEXT"
    }

    private val storage = StorageOptions.getDefaultInstance().service

    private val bucketName = "bht-demo-modeshape"

    private val fileSystemCache: FileSystemBinaryStore = TransientBinaryStore.get()

    init {
        fileSystemCache.removeValuesUnusedLongerThan(1, TimeUnit.MICROSECONDS)
    }

    override fun storeExtractedText(source: BinaryValue, extractedText: String) {
        val blobId = BlobId.of(bucketName, "${source.key}-$ExtractedTextSuffix")
        val blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build()
        storage.create(blobInfo, extractedText.toByteArray(UTF_8))
    }

    override fun storeValue(stream: InputStream, markAsUnused: Boolean): BinaryValue {
        val cachedFile = fileSystemCache.storeValue(stream, markAsUnused)
        val key = BinaryKey(cachedFile.key.toBytes())
        val blobId = BlobId.of(bucketName, key.toString())
        val blob = storage.get(blobId)
        if (blob != null && blob.exists()) {
            log.warn("Request to upload a blob that already exists! Ignoring...")
        } else {
            val blobInfo = BlobInfo.newBuilder(blobId).setContentType(cachedFile.mimeType).build()
            storage.create(blobInfo, stream.readBytes())
        }
        fileSystemCache.markAsUnused(listOf(cachedFile.key))
        return StoredBinaryValue(this, key, cachedFile.size)
    }

    override fun markAsUsed(keys: MutableIterable<BinaryKey>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStoredMimeType(binaryValue: BinaryValue): String {
        val blob = storage.get(BlobId.of(bucketName, binaryValue.key.toString()))
        return blob.contentType
    }

    override fun storeMimeType(binaryValue: BinaryValue?, mimeType: String?) {
        log.info("Requested to set mime-type on binary value. GoogleCloudStore does not have an api. Ignoring.")
    }

    override fun removeValuesUnusedLongerThan(minimumAge: Long, unit: TimeUnit?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllBinaryKeys(): MutableIterable<BinaryKey> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun markAsUnused(keys: MutableIterable<BinaryKey>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getInputStream(key: BinaryKey): InputStream {
        val blobId = BlobId.of(bucketName, key.toString())
        val blob = storage.get(blobId)
        return if(blob != null && blob.exists()) {
            Channels.newInputStream(blob.reader())
        } else {
            ByteArrayInputStream(ByteArray(0))
        }
    }

    override fun getExtractedText(source: BinaryValue): String {
        val blobId = BlobId.of(bucketName, "${source.key}-$ExtractedTextSuffix")
        val blob = storage.get(blobId)
        return if(blob != null && blob.exists()) {
            String(blob.getContent(), UTF_8)
        } else {
            ""
        }
    }
}