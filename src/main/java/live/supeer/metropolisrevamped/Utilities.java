package live.supeer.metropolisrevamped;

public class Utilities {
    public static String formattedMoney(Integer money) {
        double doubleMoney = money;
        return String.format("%,.0f",doubleMoney).replace(","," ");
    }

}
