"use strict";

function pUserLogin() {
    console.log("pUserLogin() Invoked. ");
    var formData = new FormData(document.getElementById('loginForm'));
    var url = "/user/login";

    fetch(url, {
        method: "POST",
        body: formData,
    }).then(response => {
        return response.json();
    }).then(response => {
        if (response.hasOwnProperty("Error")) {
            alert(JSON.stringify(response));
        } else {
            Cookies.set("sessionToken",response.sessionToken);
            Cookies.set("username",response.username);
            window.open("index.html","_self");
        }
    });
}
function formValidate() {
    console.log("formValidate() Invoked.");
    let valid=true;

    document.getElementById("usernameValidationText").innerHTML = "";
    document.getElementById("passwordValidationText").innerHtml = ""; //validation messages disappear if re-validated
    if (document.getElementById("username").value==""){
        document.getElementById("usernameValidationText").innerHTML = "Please enter your username.";
        valid = false;
    }
    if(!document.getElementById("password").checkValidity()) {
        document.getElementById("passwordValidationText").innerHTML = "Please enter your password.";
        valid = false;
    }
    if (valid===true) {
        pUserLogin()
    }
}