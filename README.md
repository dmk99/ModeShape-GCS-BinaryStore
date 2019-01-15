# ModeShape-GCS-BinaryStore
A BinaryStore implementation for ModeShape to allow storing binaries in Google Cloud Storage

[![](https://jitpack.io/v/mustafamotiwala/modeshape-gcs-binarystore.svg)](https://jitpack.io/#mustafamotiwala/modeshape-gcs-binarystore)
## Deployment
Add the following snippet to enable JitPack repository to your gradle file
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Then add the following dependency:
```
dependencies {
    implementation 'com.github.mustafamotiwala:modeshape-gcs-binarystore:<release-version>'
}
```
### GCS Credentials
The binary store relies on the Google Cloud SDK (more specifically the Google Cloud Storage client for java) to communicate
with Cloud Storage. Google's documentation state that for authenticating with Google's services the credentials be specified
in a very specific manner. For specific instructions see the [google documentation on the subject](https://cloud.google.com/storage/docs/reference/libraries#setting_up_authentication)
In a nutshell you need the environment variable `GOOGLE_APPLICATION_CREDENTIALS` to point to the `json` encoded credentials
exported from the GCP Console. The JVM process should have access to this variable.
**NOTE:** Make sure the credentials are for the account that has access to write to the storage/bucket.

### Modeshape Configuration
To configure `ModeShape` to use the `Modeshape-GCS-BinaryStore` use the following JSON to configuration:
```
storage: {
    binaryStorage: {
        type: custom
        classname: io.github.mmotiwala.modeshape.gcs.GoogleCloudStorageBinaryStore
        bucketName: <Your Bucket Name>
      }
}
```
**NOTE:** The BinaryStore expects the bucket where the binary artifacts are to be stored to be already created. It will
not attempt to create the bucket in case it does not exist.