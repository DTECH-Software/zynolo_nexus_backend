/**
 * Swagger UI-only helper: calls existing api-gateway login (auth_service), not a fleet API.
 */
(function () {
  function waitForUi() {
    if (window.ui && window.ui.getSystem) {
      attachLoginButton();
      return;
    }
    window.setTimeout(waitForUi, 300);
  }

  function authorizeBearer(token) {
    var system = window.ui.getSystem();
  if (!system || !system.authActions) {
      return false;
    }
    system.authActions.authorize({
      bearerAuth: {
        name: 'bearerAuth',
        schema: { type: 'http', scheme: 'bearer', bearerFormat: 'JWT' },
        value: token,
      },
    });
    return true;
  }

  function attachLoginButton() {
    if (document.getElementById('nexus-swagger-login-btn')) {
      return;
    }
    var topbar = document.querySelector('.swagger-ui .topbar-wrapper .topbar');
    if (!topbar) {
      window.setTimeout(attachLoginButton, 300);
      return;
    }

    var btn = document.createElement('a');
    btn.id = 'nexus-swagger-login-btn';
    btn.textContent = 'Nexus login';
    btn.className = 'btn';
    btn.style.marginLeft = '12px';
    btn.href = '#';
    btn.onclick = function (e) {
      e.preventDefault();
      var username = window.prompt('Username');
      if (!username) {
        return;
      }
      var password = window.prompt('Password');
      if (!password) {
        return;
      }

      var body = {
        channel: 'WEB',
        ip: '127.0.0.1',
        message: 'Fleet Swagger login',
        userAgent: navigator.userAgent || 'Swagger-UI',
        username: username,
        password: password,
      };

      fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })
        .then(function (res) {
          return res.json().then(function (json) {
            return { ok: res.ok, json: json };
          });
        })
        .then(function (result) {
          var token =
            result.json &&
            result.json.data &&
            result.json.data.tokenDetails &&
            result.json.data.tokenDetails.accessToken;
          if (!token) {
            window.alert(
              (result.json && result.json.message) || 'Login failed — check username/password'
            );
            return;
          }
          if (!authorizeBearer(token)) {
            window.alert('Token received. Use Authorize → bearerAuth and paste:\n\n' + token);
            return;
          }
          window.alert('Logged in. Bearer token is set for fleet Try it out.');
        })
        .catch(function (err) {
          window.alert('Login error: ' + (err && err.message ? err.message : err));
        });
    };

    topbar.appendChild(btn);
  }

  if (document.readyState === 'complete') {
    waitForUi();
  } else {
    window.addEventListener('load', waitForUi);
  }
})();