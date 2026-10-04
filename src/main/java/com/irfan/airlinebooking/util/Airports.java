package com.irfan.airlinebooking.util;

import com.irfan.airlinebooking.exception.NotFoundException;
import com.irfan.airlinebooking.model.Airport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalog of airports used by the flight-network generator and the search UI.
 * Includes the major airports of India plus a set of international hubs so the
 * app can offer both domestic and worldwide routes.
 * <p>
 * Coordinates are approximate (city centroids) and are only used to compute
 * great-circle distances for fare estimation.
 */
public final class Airports {

    private static final Map<String, Airport> BY_CODE = new LinkedHashMap<>();

    private Airports() {
    }

    private static void add(String code, String city, String country, double lat, double lon) {
        Airport a = new Airport(code, city, country, lat, lon);
        BY_CODE.put(code, a);
    }

    /** All airports, India first (insertion order preserved). */
    public static List<Airport> all() {
        return List.copyOf(BY_CODE.values());
    }

    public static List<Airport> byCountry(String country) {
        List<Airport> list = new ArrayList<>();
        for (Airport a : BY_CODE.values()) {
            if (a.getCountry().equalsIgnoreCase(country)) {
                list.add(a);
            }
        }
        return list;
    }

    public static Airport byCode(String code) {
        Airport a = code == null ? null : BY_CODE.get(code.toUpperCase());
        if (a == null) {
            throw new NotFoundException("Unknown airport code: " + code);
        }
        return a;
    }

    public static Airport byCity(String city) {
        for (Airport a : BY_CODE.values()) {
            if (a.getCity().equalsIgnoreCase(city)) {
                return a;
            }
        }
        throw new NotFoundException("Unknown city: " + city);
    }

    public static boolean isKnownCode(String code) {
        return code != null && BY_CODE.containsKey(code.toUpperCase());
    }

    public static String countryOf(String code) {
        return byCode(code).getCountry();
    }

    /** Great-circle distance in kilometres between two airport codes. */
    public static double distanceKm(String fromCode, String toCode) {
        Airport from = byCode(fromCode);
        Airport to = byCode(toCode);
        return haversine(from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude());
    }

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    static {
        // ---------------- India ----------------
        add("DEL", "Delhi", "India", 28.5562, 77.1000);
        add("BOM", "Mumbai", "India", 19.0896, 72.8656);
        add("BLR", "Bangalore", "India", 13.1986, 77.7066);
        add("MAA", "Chennai", "India", 12.9941, 80.1709);
        add("CCU", "Kolkata", "India", 22.6547, 88.4467);
        add("HYD", "Hyderabad", "India", 17.2403, 78.4294);
        add("AMD", "Ahmedabad", "India", 23.0722, 72.6347);
        add("COK", "Kochi", "India", 10.1520, 76.4019);
        add("GOI", "Goa", "India", 15.3808, 73.8314);
        add("PNQ", "Pune", "India", 18.5822, 73.9197);
        add("JAI", "Jaipur", "India", 26.8242, 75.8122);
        add("LKO", "Lucknow", "India", 26.7606, 80.8893);
        add("ATQ", "Amritsar", "India", 31.7096, 74.7973);
        add("VNS", "Varanasi", "India", 25.4524, 82.8593);
        add("PAT", "Patna", "India", 25.5913, 85.0880);
        add("TRV", "Thiruvananthapuram", "India", 8.4821, 76.9200);
        add("IXC", "Chandigarh", "India", 30.6735, 76.7885);
        add("BBI", "Bhubaneswar", "India", 20.2444, 85.8178);
        add("GAU", "Guwahati", "India", 26.1061, 91.5859);
        add("IXB", "Bagdogra", "India", 26.6812, 88.3286);
        add("SXR", "Srinagar", "India", 33.9871, 74.7742);
        add("IXJ", "Jammu", "India", 32.6891, 74.8375);
        add("DED", "Dehradun", "India", 30.1897, 78.1803);
        add("KNU", "Kanpur", "India", 26.4414, 80.3649);
        add("IDR", "Indore", "India", 22.7217, 75.8011);
        add("BHO", "Bhopal", "India", 23.2875, 77.3374);
        add("NAG", "Nagpur", "India", 21.0922, 79.0472);
        add("RPR", "Raipur", "India", 21.1804, 81.7388);
        add("VGA", "Vijayawada", "India", 16.5304, 80.7968);
        add("VTZ", "Visakhapatnam", "India", 17.7212, 83.2245);
        add("TIR", "Tirupati", "India", 13.6325, 79.5433);
        add("CJB", "Coimbatore", "India", 11.0300, 77.0434);
        add("IXM", "Madurai", "India", 9.8345, 78.0934);
        add("TRZ", "Tiruchirappalli", "India", 10.7654, 78.7097);
        add("IXE", "Mangalore", "India", 12.9613, 74.8901);
        add("CCJ", "Kozhikode", "India", 11.1368, 75.9553);
        add("CNN", "Kannur", "India", 11.9183, 75.5450);
        add("SVT", "Surat", "India", 21.1141, 72.7418);
        add("BDQ", "Vadodara", "India", 22.3362, 73.2263);
        add("RAJ", "Rajkot", "India", 22.3092, 70.7795);
        add("JGA", "Jamnagar", "India", 22.4730, 70.0530);
        add("BHJ", "Bhuj", "India", 23.2878, 69.6702);
        add("STV", "Bhavnagar", "India", 21.7545, 72.1851);
        add("UDR", "Udaipur", "India", 24.6177, 73.8961);
        add("JDH", "Jodhpur", "India", 26.2511, 73.0489);
        add("BKN", "Bikaner", "India", 28.0706, 73.2072);
        add("AIP", "Ajmer", "India", 26.4499, 74.6399);
        add("IXD", "Prayagraj", "India", 25.4401, 81.7340);
        add("GOP", "Gorakhpur", "India", 26.7397, 83.4497);
        add("DBR", "Darbhanga", "India", 26.1927, 85.9151);
        add("IXR", "Ranchi", "India", 23.3143, 85.3213);
        add("JSR", "Jamshedpur", "India", 22.8139, 86.1688);
        add("IXS", "Silchar", "India", 24.9129, 92.9787);
        add("IMF", "Imphal", "India", 24.7600, 93.8967);
        add("SHL", "Shillong", "India", 25.7036, 91.9786);
        add("AJL", "Aizawl", "India", 23.7469, 92.8031);
        add("DMU", "Dimapur", "India", 25.8839, 93.7711);
        add("IXI", "Lilabari", "India", 27.2955, 94.0974);
        add("TEZ", "Tezpur", "India", 26.7091, 92.7847);
        add("DIB", "Dibrugarh", "India", 27.4839, 95.0169);
        add("JRH", "Jorhat", "India", 26.7315, 94.1755);
        add("IXA", "Agartala", "India", 23.8869, 91.2404);
        add("RUP", "Rupsi", "India", 26.1404, 89.9100);

        // ---------------- Middle East / Asia ----------------
        add("DXB", "Dubai", "UAE", 25.2532, 55.3657);
        add("AUH", "Abu Dhabi", "UAE", 24.4330, 54.6511);
        add("DOH", "Doha", "Qatar", 25.2731, 51.6081);
        add("MCT", "Muscat", "Oman", 23.5933, 58.2844);
        add("RUH", "Riyadh", "Saudi Arabia", 24.9576, 46.6988);
        add("JED", "Jeddah", "Saudi Arabia", 21.6796, 39.1565);
        add("KWI", "Kuwait City", "Kuwait", 29.2266, 47.9689);
        add("BAH", "Manama", "Bahrain", 26.2708, 50.6336);
        add("DMM", "Dammam", "Saudi Arabia", 26.4711, 49.7979);
        add("SLL", "Salalah", "Oman", 17.0387, 54.0913);

        add("SIN", "Singapore", "Singapore", 1.3644, 103.9915);
        add("KUL", "Kuala Lumpur", "Malaysia", 2.7456, 101.7099);
        add("BKK", "Bangkok", "Thailand", 13.6900, 100.7501);
        add("HKT", "Phuket", "Thailand", 8.1132, 98.3169);
        add("CGK", "Jakarta", "Indonesia", -6.1256, 106.6559);
        add("DPS", "Bali", "Indonesia", -8.7482, 115.1672);
        add("MNL", "Manila", "Philippines", 14.5086, 121.0198);
        add("HKG", "Hong Kong", "China", 22.3080, 113.9185);
        add("PVG", "Shanghai", "China", 31.1443, 121.8083);
        add("PEK", "Beijing", "China", 40.0799, 116.6031);
        add("CAN", "Guangzhou", "China", 23.3924, 113.2988);
        add("NRT", "Tokyo", "Japan", 35.7720, 140.3929);
        add("HND", "Tokyo Haneda", "Japan", 35.5494, 139.7798);
        add("ICN", "Seoul", "South Korea", 37.4602, 126.4407);
        add("TPE", "Taipei", "Taiwan", 25.0777, 121.2328);
        add("KTM", "Kathmandu", "Nepal", 27.6966, 85.3591);
        add("CMB", "Colombo", "Sri Lanka", 7.1808, 79.8841);
        add("DAC", "Dhaka", "Bangladesh", 23.8433, 90.3978);
        add("MLE", "Male", "Maldives", 4.1918, 73.5291);
        add("KHI", "Karachi", "Pakistan", 24.9065, 67.1608);
        add("LHE", "Lahore", "Pakistan", 31.5216, 74.4036);

        // ---------------- Europe ----------------
        add("LHR", "London", "United Kingdom", 51.4700, -0.4543);
        add("LGW", "London Gatwick", "United Kingdom", 51.1537, -0.1821);
        add("MAN", "Manchester", "United Kingdom", 53.3537, -2.2750);
        add("CDG", "Paris", "France", 49.0097, 2.5479);
        add("AMS", "Amsterdam", "Netherlands", 52.3105, 4.7683);
        add("FRA", "Frankfurt", "Germany", 50.0379, 8.5622);
        add("MUC", "Munich", "Germany", 48.3538, 11.7861);
        add("BER", "Berlin", "Germany", 52.3667, 13.5033);
        add("ZRH", "Zurich", "Switzerland", 47.4647, 8.5492);
        add("VIE", "Vienna", "Austria", 48.1103, 16.5697);
        add("FCO", "Rome", "Italy", 41.8003, 12.2389);
        add("MXP", "Milan", "Italy", 45.6306, 8.7281);
        add("MAD", "Madrid", "Spain", 40.4936, -3.5668);
        add("BCN", "Barcelona", "Spain", 41.2974, 2.0833);
        add("LIS", "Lisbon", "Portugal", 38.7756, -9.1354);
        add("BRU", "Brussels", "Belgium", 50.9014, 4.4844);
        add("CPH", "Copenhagen", "Denmark", 55.6180, 12.6508);
        add("ARN", "Stockholm", "Sweden", 59.6519, 17.9186);
        add("OSL", "Oslo", "Norway", 60.1939, 11.1004);
        add("HEL", "Helsinki", "Finland", 60.3172, 24.9633);
        add("DUB", "Dublin", "Ireland", 53.4213, -6.2701);
        add("IST", "Istanbul", "Turkey", 41.2753, 28.7519);
        add("ATH", "Athens", "Greece", 37.9364, 23.9445);
        add("WAW", "Warsaw", "Poland", 52.1657, 20.9671);
        add("PRG", "Prague", "Czech Republic", 50.1008, 14.2600);
        add("SVO", "Moscow", "Russia", 55.9726, 37.4146);

        // ---------------- Americas ----------------
        add("JFK", "New York", "United States", 40.6413, -73.7781);
        add("EWR", "Newark", "United States", 40.6895, -74.1745);
        add("LAX", "Los Angeles", "United States", 33.9416, -118.4085);
        add("SFO", "San Francisco", "United States", 37.6213, -122.3790);
        add("ORD", "Chicago", "United States", 41.9742, -87.9073);
        add("ATL", "Atlanta", "United States", 33.6407, -84.4277);
        add("IAD", "Washington", "United States", 38.9531, -77.4565);
        add("BOS", "Boston", "United States", 42.3656, -71.0096);
        add("SEA", "Seattle", "United States", 47.4502, -122.3088);
        add("DFW", "Dallas", "United States", 32.8998, -97.0403);
        add("MIA", "Miami", "United States", 25.7959, -80.2870);
        add("YYZ", "Toronto", "Canada", 43.6777, -79.6248);
        add("YVR", "Vancouver", "Canada", 49.1967, -123.1815);
        add("MEX", "Mexico City", "Mexico", 19.4361, -99.0719);
        add("GRU", "Sao Paulo", "Brazil", -23.4356, -46.4731);
        add("EZE", "Buenos Aires", "Argentina", -34.8222, -58.5358);
        add("BOG", "Bogota", "Colombia", 4.7016, -74.1469);
        add("LIM", "Lima", "Peru", -12.0219, -77.1143);

        // ---------------- Africa / Oceania ----------------
        add("CAI", "Cairo", "Egypt", 30.1219, 31.4056);
        add("JNB", "Johannesburg", "South Africa", -26.1367, 28.2411);
        add("CPT", "Cape Town", "South Africa", -33.9715, 18.6021);
        add("NBO", "Nairobi", "Kenya", -1.3192, 36.9278);
        add("ADD", "Addis Ababa", "Ethiopia", 8.9779, 38.7993);
        add("LOS", "Lagos", "Nigeria", 6.5774, 3.3212);
        add("CMN", "Casablanca", "Morocco", 33.3675, -7.5899);
        add("MRU", "Mauritius", "Mauritius", -20.4302, 57.6836);
        add("SYD", "Sydney", "Australia", -33.9399, 151.1753);
        add("MEL", "Melbourne", "Australia", -37.6690, 144.8410);
        add("BNE", "Brisbane", "Australia", -27.3842, 153.1175);
        add("PER", "Perth", "Australia", -31.9403, 115.9672);
        add("AKL", "Auckland", "New Zealand", -37.0082, 174.7850);
    }
}
