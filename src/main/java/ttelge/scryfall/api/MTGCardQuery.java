package ttelge.scryfall.api;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Top-level class, provides a static way to search for cards.
 * @author ForOhForError
 */

@SuppressWarnings({"unused", "DuplicatedCode"})
public class MTGCardQuery {

	private static final String API_URI = "https://api.scryfall.com";
	private static final JSONParser JSON_PARSER = new JSONParser();

	/**
	 * Returns a list of card objects containing all cards matching any
	 * of the card names passed as an argument. Works in as few API calls
	 * as possible.
	 * @param cardnames The collection of cardnames to get a list of objects from
	 * @param listDuplicates If true, the returned list will contain all
	 * editions of any card in the input collection.
	 * @return A list of card objects that match the query. 
	 */
	public static ArrayList<Card> toCardList(Collection<String> cardnames, boolean listDuplicates)
	{
		ArrayList<Card> result = new ArrayList<>();
		for(String cardname:cardnames)
		{
			String query;
			if(listDuplicates)
			{
				query ="++!\""+cardname+"\"";
			}
			else
			{
				query ="!\""+cardname+"\"";
			}
			result.addAll(search(query));
		}
		return result;
	}
	
	/**
	 * @return A list of all sets in magic's history.
	 */
	public static ArrayList<Set> getSets()
	{
		ArrayList<Set> s = new ArrayList<>();
		try{
			URL url = new URL("https://api.scryfall.com/sets");
			URLConnection conn = url.openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), StandardCharsets.UTF_8));


			StringBuilder json = new StringBuilder();
			String line = "";
			while(line != null)
			{
				json.append(line);
				line = in.readLine();
			}

			JSONObject root = null;
			try {
				root = (JSONObject)MTGCardQuery.JSON_PARSER.parse(json.toString());
			} catch (ParseException ignored) {
			}

			in.close();

			assert root != null;
			JSONArray sets = (JSONArray)root.get("data");

			for (Object set : sets) {
				JSONObject setData = ((JSONObject) set);
				s.add(new Set(setData));
			}
		}catch(IOException ignored){

		}
		return s;
	}
	
	/**
	 * Returns a list of card objects that match the query. 
	 * The query should be formatted using scryfall's syntax:
	 * https://www.scryfall.com/docs/syntax
	 * @param query The query to match cards to
	 * @return A list of card objects that match the query. 
	 */
	public static ArrayList<Card> search(String query)
	{
		String escapedQuery = "";
		try{
			escapedQuery = URLEncoder.encode(query,"UTF-8");
		}catch(IOException ignored){}
		String uri = API_URI+"/cards/search?unique=prints&q="+escapedQuery;

		return getCardsFromURI(uri);
	}

	/**
	 * Returns a single card object representing the card with the given ID
	 * @param id The URI to pull data fromScryfall ID of the card
	 * @return A single card object representing the card with the given ID
	 */
	public static Card getCardByScryfallId(String id) throws IOException
	{
		URL url = new URL("https://api.scryfall.com/cards/"+id);
		URLConnection conn = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream(), StandardCharsets.UTF_8));

		StringBuilder json = new StringBuilder();
		String line = "";
		while(line != null)
		{
			json.append(line);
			line = in.readLine();
		}

		JSONObject root = null;
		try {
			root = (JSONObject)MTGCardQuery.JSON_PARSER.parse(json.toString());
		} catch (ParseException ignored) {
		}

		in.close();

		return new Card(root);
	}
	
	/**
	 * Returns a single card object from the given URI
	 * @param uri The URI to pull data from
	 * @return A single card object from the uri
	 */
	public static Card getCardFromURI(String uri) throws IOException
	{
		String escapedQuery = "";
		try{
			escapedQuery = URLEncoder.encode(uri,"UTF-8");
		}catch(IOException ignored){}
		URL url = new URL(escapedQuery);
		URLConnection conn = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream(), StandardCharsets.UTF_8));

		StringBuilder json = new StringBuilder();
		String line = "";
		while(line != null)
		{
			json.append(line);
			line = in.readLine();
		}

		JSONObject root = null;
		try {
			root = (JSONObject)MTGCardQuery.JSON_PARSER.parse(json.toString());
		} catch (ParseException ignored) {
		}

		in.close();

		return new Card(root);
	}

	/**
	 * Returns a list of card objects from the given URI
	 * @param uri The URI to pull data from
	 * @return A list of card objects from the uri
	 */
	public static ArrayList<Card> getCardsFromURI(String uri)
	{
		ArrayList<Card> cards = new ArrayList<Card>();
		try{
			URL url = new URL(uri);
			URLConnection conn = url.openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), StandardCharsets.UTF_8));


			StringBuilder json = new StringBuilder();
			String line = "";
			while(line != null)
			{
				json.append(line);
				line = in.readLine();
			}

			JSONObject root = null;
			try {
				root = (JSONObject)MTGCardQuery.JSON_PARSER.parse(json.toString());
			} catch (ParseException ignored) {
			}

			in.close();

			assert root != null;
			JSONArray jsonCards = (JSONArray)root.get("data");

			for (Object jsonCard : jsonCards) {
				JSONObject cardData = ((JSONObject) jsonCard);
				cards.add(new Card(cardData));
			}

			if(root.containsKey("has_more") && (Boolean) root.get("has_more")){
				String next = (String)root.get("next_page");
				try {
					//Requested wait time between queries
					Thread.sleep(50);
				} catch (InterruptedException ignored) { }
				cards.addAll(getCardsFromURI(next));
			}
		}catch(IOException ignored){

		}

		return cards;
	}
}