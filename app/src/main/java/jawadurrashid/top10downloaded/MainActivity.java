package jawadurrashid.top10downloaded;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        while (true){                  // Loop prevents onCreate method from finishing; will make app freeze
            int x = 5;
        }
    }
}
