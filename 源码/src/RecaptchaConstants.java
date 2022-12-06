import java.io.*;


public class RecaptchaConstants {

    public static String SECRET_KEY ="6LeSHMEaAAAAAIsbNbrR_rUNt9Y547XsN_qt0N8U";
/*
    public String getSecretKey() {
        return this.SECRET_KEY;
    }

    public void setSecretKeyFromEnv() {
        File recaptchaKeyFile = new File (".env.txt");
        System.out.println("test");

        //File currentDirFile = new File(".");
        String currentDir = recaptchaKeyFile.getAbsolutePath();
        System.out.println(currentDir);



        try (BufferedReader br = new BufferedReader(new FileReader(recaptchaKeyFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] keyValue = line.split("=");
                System.out.println("keyValue[0]: " + keyValue[0]);
                System.out.println("keyValue[1]: " + keyValue[1]);

                if (keyValue[0].equals("SECRET_KEY")) {
                    SECRET_KEY = keyValue[1];
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
}
