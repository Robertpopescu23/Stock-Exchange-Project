import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
public class TestAtlas {
    public static void main(String[ ] args)
    {
        try(var client = MongoClients.create(new ConnectionString(
                "mongodb+srv://robertDB:CEBPproject123@cebp-project.afjjmfq.mongodb.net/TradingDB?retryWrites=true&w=majority&tls=true&appName=CEBP-Project"))) {
            System.out.println("Connected!");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
