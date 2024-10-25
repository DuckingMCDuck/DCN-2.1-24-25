import com.google.gson.Gson;
import nl.avans.JsonResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;

public class ReaderProtocol {
    private static final int WAITING = 0;
    private static final int SENTISBN = 1;
    private static final int SENTKNOCKKNOCK = 1;
    private static final int SENTCLUE = 2;
    private static final int ANOTHER = 3;

    private static final int NUMJOKES = 5;

    private int state = WAITING;
    private int currentJoke = 0;
    private String[] clues = {"Turnip", "Little Old Lady", "Atch", "Who", "Who"};
    private String[] answers = {"Turnip the heat, it's cold in here!",
            "I didn't know you could yodel!",
            "Bless you!",
            "Is there an owl in here?",
            "Is there an echo in here?"};

    public String processInput(String theInput) throws IOException {
        String theOutput = null;

        if (state == WAITING) {
            theOutput = "Type ISBN to get the book information";
            state = SENTISBN;
        } else if (state == SENTISBN) {
            URL bookInfo = new URL("https://www.googleapis.com/books/v1/volumes?q=isbn:" + theInput);
            InputStream input = bookInfo.openStream();
            Reader reader = new InputStreamReader(input, "UTF-8");
            JsonResult result = new Gson().fromJson(reader, JsonResult.class);

            String Title = result.getBook().getBookDetail().getTitle();
            if (Title == null) {
                theOutput = "No book found with ISBN: " + theInput;
                state = WAITING;
                return theOutput;
            }
            String SubTitle = result.getBook().getBookDetail().getSubTitle();
            if (SubTitle == null) {
                SubTitle = "No subtitle";
            }
            List Authors = result.getBook().getBookDetail().getAuthors();
            if (Authors == null) {
                Authors.add("No authors");
            }

            theOutput = "Title: " + Title + " | "
                    + "Subtitle: " + SubTitle + " | "
                    + "Authors: " + Authors;
            state = WAITING;

        }
        return theOutput;
    }
}

