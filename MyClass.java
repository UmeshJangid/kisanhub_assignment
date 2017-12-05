import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
/**
 * @author Umesh Jangid Dec 1, 2017 4:52:35 PM
 */
public class MyClass {
	// Base url
	private static String BASE_URL = "https://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/";
	public static ArrayList<ModalClass> listLines = new ArrayList<ModalClass>();
	private static String csv_path = Util.getHomePath() + "weather.csv";
	public static String url_mid[] = { "Tmax/date/", "Tmin/date/", "Tmean/date/", "Sunshine/date/", "Rainfall/date/" };
	public static String[] str = { "", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV",
			"DEC" };

	public static void main(String[] args) throws Exception {
		// File Creation
		for (int i = 0; i < 4; i++) {// Country
			for (int j = 0; j < 5; j++) {// Key@param
				File file = new File(Util.getHomePath() + Util.country[i] + "_" + Util.keyParams[j] + ".txt");
				if (!file.exists()) {
					file.createNewFile();
					downloadFile(file, url_mid[j], Util.country[i] + ".txt");
				}
				System.out.println("Path :" + Util.getHomePath() + Util.country[i] + "_" + Util.keyParams[j] + ".txt");
			}
		}

	}

	// Method: Download file from given url one by one 
	private static void downloadFile(File file, String topic_url, String country) throws Exception {
		file.createNewFile();
		System.out.println("URL :" + BASE_URL + topic_url + country);
		URL url = new URL(BASE_URL + topic_url + country);
		InputStream webIS = url.openStream();
		FileOutputStream fo = new FileOutputStream(file);
		int c = 0;
		do {
			c = webIS.read();
			if (c != -1) {
				fo.write((byte) c);
			}
		} while (c != -1);
		webIS.close();
		// Step 1: When file made than covert the text data to string and
		// replace all entity with single space.
		String coString = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
		coString = coString.replaceAll("( )+", " ");
		// Step 1.1: Now replace extra space(single space earlier)with three
		// space so that your file looks same as it is on server
		coString = coString.replaceAll("( )+", "   ");
		// Step 2: when we get string overwrite that file in our format.
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(coString);
		fileWriter.flush();
		fileWriter.close();
		fo.close();
		//Step 3: Read file data which is download.
		readFile(file, topic_url, country);
	}
	
	//Method: readFile : Read file data insert into our bean/pojo class.
	public static void readFile(File file_to_convert, String topic, String country) throws Exception {
		try {
			LineNumberReader lineReader = new LineNumberReader(new FileReader(file_to_convert));
			String lineText = null;
			while ((lineText = lineReader.readLine()) != null) {
				ModalClass modalClass = null;
				int lineNumber = lineReader.getLineNumber();
				long count = Files.lines(Paths.get(file_to_convert.getAbsolutePath())).count();
				if (lineNumber >= 9 && lineNumber <= count) {
					String year = "N/A";
					// System.out.print(lineNumber + ": " + lineText);
					String[] words_1 = lineText.trim().split("");
					year = words_1[0];
					// System.out.println(words_1[0]);
					String[] words = lineText.trim().split("   ");
					year = words[0];
					int c = 0;
					for (int i = 1; i < 13; i++) {
						modalClass = new ModalClass();
						String string_country[] = country.split("\\.");
						String string_key[] = topic.split("/");
						if (string_country != null) {
							modalClass.setRegionCode(string_country[0]);
						}
						/*
						 * switch (string_key[0].trim().toString()) { case
						 * "TMax": modalClass.setWeatherParam("MaxTemp"); break;
						 * case "TMin": modalClass.setWeatherParam("MinTemp");
						 * break; case "TMean":
						 * modalClass.setWeatherParam("MeanTemp"); break; case
						 * "Sunshine": modalClass.setWeatherParam("Sunshine");
						 * break; case "Rainfall":
						 * modalClass.setWeatherParam("Rainfall"); break;
						 * default: break; }
						 */

						if (string_key[0].equals("Tmax")) {
							modalClass.setWeatherParam("MaxTemp");
						} else if (string_key[0].equals("Tmin")) {
							modalClass.setWeatherParam("MinTemp");
						} else if (string_key[0].equals("Tmean")) {
							modalClass.setWeatherParam("MeanTemp");
						} else if (string_key[0].equals("Sunshine")) {
							modalClass.setWeatherParam("Sunshine");
						} else if (string_key[0].equals("Rainfall")) {
							modalClass.setWeatherParam("Rainfall");
						} else {
							System.out.println("INVALID COLOR CODE");
						}
						modalClass.setYear(year);
						modalClass.setKeyMonth(str[i]);
						modalClass.setValue(words[i].trim().isEmpty() ? "N/A" : words[i].trim());
						listLines.add(modalClass);
					}
				}
				if (listLines.size() > 0) {
					writeCSV(listLines);
				}
			}
			for (ModalClass modalClass : listLines) {
				System.out.println(modalClass.getRegionCode() + "," + modalClass.getWeatherParam() + ","
						+ modalClass.getYear() + "," + modalClass.getKeyMonth() + "," + modalClass.getValue());
			}
			lineReader.close();
		} catch (IOException ex) {
			System.err.println(ex);
		}

	}

	//Method: WriteCSV : Writes data into single file one by one
	public static void writeCSV(ArrayList<ModalClass> arrayList) throws Exception {
		FileWriter writer = new FileWriter(csv_path);
		int size = arrayList.size();
		for (int i = 0; i < size; i++) {
			ModalClass modalClass = arrayList.get(i);
			writer.write(modalClass.getRegionCode() + "," + modalClass.getWeatherParam() + "," + modalClass.getYear()
					+ "," + modalClass.getKeyMonth() + "," + modalClass.getValue());
			if (i < size - 1)// This prevent creating a blank like at the end of
								// the file**
				writer.write("\n");
		}
		writer.close();
		writer.close();
	}
}
