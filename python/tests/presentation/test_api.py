import pytest
from assertpy import assert_that


class TestCreateGreeting:
    def test_returns_200_with_greeting(self, client):
        response = client.post("/greetings", json={"name": "Ada", "language": "en"})

        assert_that(response.status_code).is_equal_to(200)
        assert_that(response.json()).is_equal_to({"greeting": "Hello, Ada!", "language": "en"})

    def test_language_is_case_insensitive_and_returns_canonical_code(self, client):
        response = client.post("/greetings", json={"name": "Ada", "language": "ES-co"})

        assert_that(response.status_code).is_equal_to(200)
        assert_that(response.json()).is_equal_to({"greeting": "¡Hola, Ada!", "language": "es"})

    def test_unsupported_language_returns_400(self, client):
        response = client.post("/greetings", json={"name": "Ada", "language": "xx"})

        assert_that(response.status_code).is_equal_to(400)
        assert_that(response.json()["detail"]).contains("xx")

    @pytest.mark.parametrize(
        "payload",
        [
            {},
            {"name": "Ada"},
            {"language": "en"},
            {"name": "", "language": "en"},
            {"name": "   ", "language": "en"},
            {"name": "Ada", "language": ""},
            {"name": 123, "language": "en"},
            {"name": None, "language": "en"},
        ],
        ids=[
            "empty-body",
            "missing-language",
            "missing-name",
            "empty-name",
            "blank-name",
            "empty-language",
            "name-not-string",
            "name-null",
        ],
    )
    def test_invalid_body_returns_400_not_422(self, client, payload):
        response = client.post("/greetings", json=payload)

        assert_that(response.status_code).is_equal_to(400)
        assert_that(response.json()).contains_key("detail")

    def test_malformed_json_returns_400(self, client):
        response = client.post(
            "/greetings",
            content="{not json",
            headers={"Content-Type": "application/json"},
        )

        assert_that(response.status_code).is_equal_to(400)


class TestGetTypicalGreetings:
    def test_returns_200_with_greetings(self, client):
        response = client.get("/greetings", params={"lang": "es"})

        assert_that(response.status_code).is_equal_to(200)
        assert_that(response.json()).is_equal_to(
            {"language": "es", "greetings": ["Hola", "Buenos días"]}
        )

    def test_language_is_normalized(self, client):
        response = client.get("/greetings", params={"lang": "EN"})

        assert_that(response.status_code).is_equal_to(200)
        assert_that(response.json()["language"]).is_equal_to("en")

    def test_missing_lang_returns_400(self, client):
        response = client.get("/greetings")

        assert_that(response.status_code).is_equal_to(400)
        assert_that(response.json()["detail"][0]["loc"]).is_equal_to(["query", "lang"])

    @pytest.mark.parametrize("lang", ["xx", ""])
    def test_unsupported_or_empty_lang_returns_400(self, client, lang):
        response = client.get("/greetings", params={"lang": lang})

        assert_that(response.status_code).is_equal_to(400)


class TestOpenApiContract:
    def test_operation_ids_match_spec(self, client):
        paths = client.get("/openapi.json").json()["paths"]["/greetings"]

        assert_that(paths["post"]["operationId"]).is_equal_to("createGreeting")
        assert_that(paths["get"]["operationId"]).is_equal_to("getTypicalGreetings")
