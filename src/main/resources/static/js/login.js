var onoff = true;
var confirm = document.getElementsByClassName("confirm")[0];
var user = document.getElementById("user");
var pwd = document.getElementById("pwd");
var con_pass = document.getElementById("confirm-passwd");
var name_c = document.getElementById("title");
var name = name_c.innerHTML.split("");
name_c.innerHTML = "";
for (i = 0; i < name.length; i++) if (name[i] != ",") name_c.innerHTML += "<i>" + name[i] + "</i>";

function hint() {
    let hit = document.getElementById("hint");
    hit.style.display = "block";
    setTimeout(function () {
        hit.style.opacity = 1
    }, 0);
    setTimeout(function () {
        hit.style.opacity = 0
    }, 2000);
    setTimeout(function () {
        hit.style.display = "none"
    }, 3000)
}

document.onkeydown = function (e) {
    if (e.keyCode === 13) {
        document.getElementById("login_enter").onclick()
    }
};

function login() {
    if (onoff) {
        if (user.value !== "" && pwd.value !== "") {
            let url = "/api/login";
            let data = new FormData();
            let request = new XMLHttpRequest();
            request.open("post", url, true);
            data.append("username", user.value);
            data.append("password", pwd.value);
            request.onreadystatechange = function () {
                if (this.readyState === 4) {
                    if (this.responseText === "登陆失败") {
                        hint()
                    } else {
                        window.location.href = this.responseText
                    }
                }
            };
            request.send(data)
        }
    } else {
        let status = document.getElementById("status").getElementsByTagName("i");
        confirm.style.height = 0;
        status[0].style.top = 0;
        status[1].style.top = 35 + "px";
        onoff = !onoff
    }
}

function signin() {
    let status = document.getElementById("status").getElementsByTagName("i");
    let hit = document.getElementById("hint").getElementsByTagName("p")[0];
    if (onoff) {
        confirm.style.height = 51 + "px";
        status[0].style.top = 35 + "px";
        status[1].style.top = 0;
        onoff = !onoff
    } else {
        if (!/^[A-Za-z0-9]+$/.test(user.value)) {
            hit.innerHTML = "账号只能为英文和数字"
        } else if (user.value.length < 6) {
            hit.innerHTML = "账号长度必须大于6位"
        } else if (pwd.value.length < 6) {
            hit.innerHTML = "密码长度必须大于6位"
        } else if (pwd.value !== con_pass.value) {
            hit.innerHTML = "两次密码不相等"
        } else if (pwd.value === con_pass.value) {
            let url = "/api/register";
            let data = new FormData();
            let request = new XMLHttpRequest();
            request.open("post", url, true);
            data.append("username", user.value);
            data.append("password", pwd.value);
            data.append("conPassword", con_pass.value);
            request.onreadystatechange = function () {
                if (this.readyState === 4) {
                    if (this.responseText === "该账号已存在") {
                        hit.innerHTML = this.responseText
                    } else if (this.responseText === "账号注册成功") {
                        hit.innerHTML = this.responseText + "，两秒后自动刷新页面";
                        setTimeout("window.location.reload()", 2000)
                    } else {
                        hit.innerHTML = this.responseText
                    }
                }
            };
            request.send(data)
        }
        hint()
    }
}