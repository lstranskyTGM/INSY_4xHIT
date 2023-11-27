function fileLoad() {
    var fileInput = document.getElementById("fileInput");
    var formData = new FormData();
    formData.append('fileInput', fileInput.files[0]);

    fetch('/file_load', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            document.getElementById("jsonTextArea").value = JSON.stringify(JSON.parse(data.data), null, 2);
            alert("File loaded successfully!");
        } else {
            alert("Error loading file: " + data.error);
        }
    })
    .catch(error => {
        alert("Error loading file: " + error);
    });
}

function fileSave() {
    try {
        var jsonData = JSON.parse(document.getElementById("jsonTextArea").value);
        var data = "text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(jsonData));
        var dataDownload = document.createElement('a');
        dataDownload.href = 'data:' + data;
        dataDownload.download = 'data.json';
        var container = document.getElementById("downloadContainer");
        container.innerHTML = "";
        var downloadButton = document.createElement('button');
        downloadButton.innerHTML = "Download JSON";
        downloadButton.onclick = function() {
            dataDownload.click();
        }
        container.appendChild(downloadButton);
        alert("File valid!");
    } catch (error) {
        alert("Invalid JSON data!");
    }
}
