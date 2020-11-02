echo '{"login": "tojatos", "password": "password"}' | https --verify=no -j POST :8080/api/login
echo "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoidG9qYXRvcyJ9.hE0HnomJj7_LQGRBwQoTggEycKT61Us8LkyjycaRTg4" | https --verify=no -j GET :8080/api/secret
