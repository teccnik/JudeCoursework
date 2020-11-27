function formValidate() {
    console.log("formValidate() Invoked.");
    let valid=true;
    document.getElementById("usernameValidationText").innerHTML ="";
    document.getElementById("emailValidationText").innerHTML="";
    document.getElementById("passwordValidationText").innerHTML="";

    if (document.getElementById("username").value=="") {
        document.getElementById("usernameValidationText").innerHTML = "Please enter a username.";
        valid = false;
    }
    if (!document.getElementById("email").checkValidity()) {
        valid= false;
        document.getElementById('email').value='';
        document.getElementById("emailValidationText").innerHTML = "Please enter a valid email.";
    }
    if (!document.getElementById("password").checkValidity()) {
        valid = false;
        document.getElementById("passwordValidationText").innerHTML = "Please enter a password between 8 and 12 characters.";
    }
    if (valid===true) {
        postUserAdd();
    }
}
function postUserAdd() {
    console.log("postUserAdd() Invoked.");
    var formData = new FormData(document.getElementById('newUserForm'));
    var url = "/user/new";

    fetch(url, {
        method: "POST", body: formData,
    }).then(response=> {
        return response.json();
    }).then(response=> {
        if (response.hasOwnProperty("Error")) {
            alert("An error occurred.");
        } else {
            alert("New account created! You may now log in.");
            window.open("login.html","_self");
        }
    });
}