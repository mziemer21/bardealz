package yelp;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * Example for accessing the Yelp API.
 */
public class Yelp {

	private OAuthService service;
	private Token accessToken;

	/**
	 * Setup the Yelp API OAuth credentials.
	 * 
	 * OAuth credentials are available from the developer site, under Manage API
	 * access (version 2 API).
	 * 
	 * @param consumerKey
	 *            Consumer key
	 * @param consumerSecret
	 *            Consumer secret
	 * @param token
	 *            Token
	 * @param tokenSecret
	 *            Token secret
	 */
	public Yelp(String consumerKey, String consumerSecret, String token, String tokenSecret) {
		this.service = new ServiceBuilder().provider(YelpApi2.class).apiKey(consumerKey).apiSecret(consumerSecret).build();
		this.accessToken = new Token(token, tokenSecret);
	}

	/**
	 * Search with term and location.
	 * 
	 * @param term
	 *            Search term
	 * @param latitude
	 *            Latitude
	 * @param longitude
	 *            Longitude
	 * @return JSON string response
	 */
	public String search(String term, double latitude, double longitude, String distance, int sortMode, Integer loadMore) {

		Double swLat, swLng, neLat, neLng;
	    Double half_side_in_km = Double.parseDouble(distance)/1000;// * 1.609344;
	    
	    Double lat = Math.toRadians(latitude);
	    Double lng = Math.toRadians(longitude);

	    Integer radius = 6371;
	    // Radius of the parallel at given latitude
	    Double parallel_radius = radius*Math.cos(lat);

	    Double lat_min = lat - half_side_in_km/radius;
	    Double lat_max = lat + half_side_in_km/radius;
	    Double lon_min = lng - half_side_in_km/parallel_radius;
	    Double lon_max = lng + half_side_in_km/parallel_radius;

	    swLat = Math.toDegrees(lat_min);
	    swLng = Math.toDegrees(lon_min);
	    neLat = Math.toDegrees(lat_max);
	    neLng = Math.toDegrees(lon_max);
		
		
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
		request.addQuerystringParameter("category_filter", "bars");
		if(!term.equals("")){
			request.addQuerystringParameter("term", term);
		}
		//request.addQuerystringParameter("ll", latitude + "," + longitude);
		request.addQuerystringParameter("bounds", swLat + "," + swLng + "|" + neLat + "," + neLng);
		request.addQuerystringParameter("limit", "15");
		request.addQuerystringParameter("radius_filter", distance);
		request.addQuerystringParameter("sort", String.valueOf(sortMode));
		if (loadMore > 0) {
			request.addQuerystringParameter("offset", String.valueOf(loadMore));
		}
		
		service.signRequest(accessToken, request);
		Response response = request.send();
		return response.getBody();
	}

	public String businessSearch(String id) {
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/business/" + id);
		service.signRequest(accessToken, request);
		Response response = request.send();
		return response.getBody();
	}
}
