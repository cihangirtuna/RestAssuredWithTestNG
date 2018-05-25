package TestPackage;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

public class TestClass {

	@DataProvider
	public String[] getAlpha2CodeExistent() {
		return new String[] { "US", "DE", "GB" };
	}

	@DataProvider
	public String[] getAlpha2CodeInExistent() {
		return new String[] { "TDD", "BDD", "TTD" };
	}

	public static ValidatableResponse getRequest(String endpoint) {
		return given().when().get(endpoint).then().contentType(ContentType.JSON);
	}

	@Test(dataProvider = "getAlpha2CodeExistent")
	public void getAllCountries(String alpha2Code) {

		Boolean isTrue = false;
		ValidatableResponse response = getRequest("http://services.groupkt.com/country/get/all");
		response.statusCode(200);

		List<Object> listObject = response.extract().jsonPath().getList("RestResponse.result.alpha2_code");

		for (Object o : listObject) {
			if (o.toString().equalsIgnoreCase(alpha2Code)) {
				isTrue = true;
			}
		}
		Assert.assertTrue(isTrue);
	}

	@Test(dataProvider = "getAlpha2CodeExistent")
	public void getEachCountry(String alpha2Code) {
		ValidatableResponse response = getRequest("http://services.groupkt.com/country/get/iso2code/" + alpha2Code);
		response.statusCode(200);

		response.body("RestResponse.result.alpha2_code", Matchers.equalToIgnoringCase(alpha2Code));
		response.body("RestResponse.result.name", Matchers.notNullValue());
		response.body("RestResponse.result.alpha3_code", Matchers.notNullValue());
	}

	@Test(dataProvider = "getAlpha2CodeInExistent")
	public void getInexistentCountries(String alpha2Code) {
		ValidatableResponse response = getRequest("http://services.groupkt.com/country/get/iso2code/" + alpha2Code);
		response.statusCode(200);

		String message = response.extract().jsonPath().get("RestResponse.messages").toString();
		assertTrue(message.contains("No matching country found for requested code"));
	}

	@Test
	public void postInExistentCountries() {
		RestAssured.baseURI = "http://services.groupkt.com/country/createCountry";
		String json = "{name: 'Test Country',alpha2_code: 'TC',alpha3_code: 'TCY'}";
		ValidatableResponse response = RestAssured.given().when().header("Content-Type", "application/json").body(json)
				.post().then();
		response.statusCode(200);
		response.body("message", Matchers.equalToIgnoringCase("Success"));
	}

}
