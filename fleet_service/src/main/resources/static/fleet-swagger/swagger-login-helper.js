/**
 * Swagger UI-only helper: calls existing api-gateway login (auth_service), not a fleet API.
 * Places a "Login" button beside Authorize.
 */
(function () {
  var tries = 0;
  var maxTries = 80;

  function waitAndAttach() {
    if (document.getElementById('nexus-swagger-login-btn')) {
      return;
    }
    tries += 1;
    if (tries > maxTries) {
      return;
    }
    var host = findAuthorizeHost();
    if (!host) {
      window.setTimeout(waitAndAttach, 250);
      return;
    }
    host.appendChild(buildLoginButton());
  }

  function findAuthorizeHost() {
    var authorizeBtn =
      document.querySelector('.swagger-ui .auth-wrapper .authorize') ||
      document.querySelector('.swagger-ui button.authorize') ||
      document.querySelector('.swagger-ui .authorization__btn');
    if (authorizeBtn && authorizeBtn.parentElement) {
      return authorizeBtn.parentElement;
    }
    var schemeContainer = document.querySelector('.swagger-ui .scheme-container');
    if (schemeContainer) {
      return schemeContainer;
    }
    var info = document.querySelector('.swagger-ui .information-container');
    if (info) {
      return info;
    }
    return null;
  }

  function authorizeBearer(token) {
    if (!window.ui || !window.ui.getSystem) {
      return false;
    }
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

  function buildLoginButton() {
    var btn = document.createElement('button');
    btn.id = 'nexus-swagger-login-btn';
    btn.type = 'button';
    btn.textContent = 'Login';
    btn.title = 'Login via /api/v1/auth/login and set Bearer token';
    btn.className = 'btn authorize';
    btn.style.marginLeft = '10px';
    btn.style.background = '#49cc90';
    btn.style.borderColor = '#49cc90';
    btn.style.color = '#fff';
    btn.onclick = function (e) {
      e.preventDefault();
      e.stopPropagation();
      doLogin();
    };
    return btn;
  }

  function doLogin() {
    var username = window.prompt('Username', 'superadmin');
    if (!username) {
      return;
    }
    var password = window.prompt('Password');
    if (!password) {
      return;
    }

    var body = {
      channel: 'OP',
      ip: '127.0.0.1',
      message: 'Fleet Swagger login',
      userAgent: navigator.userAgent || 'Swagger-UI',
      username: username.trim(),
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
          window.alert((result.json && result.json.message) || 'Login failed — check username/password');
          return;
        }
        if (!authorizeBearer(token)) {
          window.alert('Token received. Paste into Authorize → bearerAuth:\n\n' + token);
          return;
        }
        window.alert('Logged in. Bearer token set — use Try it out on fleet APIs.');
      })
      .catch(function (err) {
        window.alert('Login error: ' + (err && err.message ? err.message : err));
      });
  }

  if (document.readyState === 'complete') {
    waitAndAttach();
  } else {
    window.addEventListener('load', waitAndAttach);
  }
})();