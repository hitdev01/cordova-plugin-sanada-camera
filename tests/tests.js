/* jshint jasmine: true */
exports.defineAutoTests = function () {
    describe('Echo (navigator.sanadacamera.start)', function () {
        it("Echo.spec.1 should exist", function () {
            expect(navigator.sanadacamera).toBeDefined();
            expect(navigator.sanadacamera.start).toBeDefined();
        });
    });
};

exports.defineManualTests = function(contentEl, createActionButton) {
    contentEl.innerHTML = '<div id="camerastart"></div><br /><img id="image" height="150px" src=""></img>';
    createActionButton('camera start', function() {
      var image = document.getElementById('image');
      var success = function(result) {
        image.src = result;
      };
      var error = function(err) {
        alert(err);
      };

      navigator.sanadacamera.start(success, error);
    }, "camerastart");
};
