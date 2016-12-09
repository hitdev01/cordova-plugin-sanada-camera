# cordova-plugin-sample
cordova plugin sample.
A camera that can be shot with Yukimura Sanada.

## Installation
```
cordova plugin add https://github.com/hitdev01/cordova-plugin-sanada-camera.git
```
## Supported Platforms
* Android

## Methods
* navigator.sanadacamera.start

## Example
### navigator.sanadacamera.start

```JavaScript
function onSuccess(uri) {
    // URI of photo taken
    alert(uri);
}

function onError() {
    alert('onError!');
}

navigator.sanadacamera.start(onSuccess, onError);
```


## Testing the this plugin itself

### JavaScript syntax check test
`npm test`

### Actual machine test
* create cordova project

```bash
cordova create cordova-plugin-sample-test
cordova platform add android
```

* add test-framework plugin & this plugin & this plugin test

```bash
cordova plugin add http://git-wip-us.apache.org/repos/asf/cordova-plugin-test-framework.git
cordova plugin add https://github.com/hitdev01/cordova-plugin-sanada-camera.git
cordova plugin add https://github.com/hitdev01/cordova-plugin-sanada-camera.git#:/tests
```

* change config.xml content

```xml
<!--content src="index.html" /-->
<content src="cdvtests/index.html" />
```

* run test

android (use AndroidSDK)

`cordova run android`
