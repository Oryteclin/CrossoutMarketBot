package ovh.akio.cmb;

public class Main {

    public static void main(String[] args){

        boolean beta = false;
        for (String arg : args) {
            if(arg.equalsIgnoreCase("beta")) {
                beta = true;
            }
        }

        new CrossoutMarketBot(beta);
    }

}
