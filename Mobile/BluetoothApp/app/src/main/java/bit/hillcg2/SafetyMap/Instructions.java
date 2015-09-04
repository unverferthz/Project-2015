package bit.hillcg2.SafetyMap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;

public class Instructions extends Activity {

    private Button btnBack;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        btnBack = (Button)findViewById(R.id.btnInstructionsBack);
        btnBack.setOnClickListener(new backToHome());

        scrollView = (ScrollView)findViewById(R.id.scrollView);


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenHeight = size.y;

        int scrollViewHeight = screenHeight - btnBack.getHeight() - 460;
        scrollView.getLayoutParams().height = scrollViewHeight;
    }

    public class backToHome implements OnClickListener{

        @Override
        public void onClick(View v) {
            Intent homeIntent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }
}
