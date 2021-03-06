package colibri.dev.com.colibritweet.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import colibri.dev.com.colibritweet.R;
import colibri.dev.com.colibritweet.adapter.UsersAdapter;
import colibri.dev.com.colibritweet.network.HttpClient;
import colibri.dev.com.colibritweet.pojo.Tweet;
import colibri.dev.com.colibritweet.pojo.User;

public class SearchUsersActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private UsersAdapter usersAdapter;
    private Toolbar toolbar;
    private EditText queryEditText;
    private Button searchButton;

    private SwipeRefreshLayout swipeRefreshLayout;

    HttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        initRecyclerView();

        toolbar = findViewById(R.id.toolbar);
        queryEditText = toolbar.findViewById(R.id.query_edit_text);
        searchButton = toolbar.findViewById(R.id.search_button);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchButton.setOnClickListener(v -> searchUsers());

        queryEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchUsers();
                return true;
            }
            return false;
        });

        httpClient = new HttpClient();

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> searchUsers());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void initRecyclerView() {
        usersRecyclerView = findViewById(R.id.users_recycler_view);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        UsersAdapter.OnUserClickListener onUserClickListener = user -> {
            Intent intent = new Intent(SearchUsersActivity.this, UserInfoActivity.class);
            intent.putExtra(UserInfoActivity.USER_ID, user.getId());
            startActivity(intent);
        };
        usersAdapter = new UsersAdapter(onUserClickListener);
        usersRecyclerView.setAdapter(usersAdapter);
    }

    private void searchUsers() {
        final String query = queryEditText.getText().toString();
        if (query.length() == 0) {
            Toast.makeText(SearchUsersActivity.this, R.string.not_enough_symbols_msg, Toast.LENGTH_SHORT).show();
            return;
        }
        new SearchUsersAsyncTask().execute(query);
    }

    @SuppressLint("StaticFieldLeak")
    private class SearchUsersAsyncTask extends AsyncTask<String, Integer, Collection<User>> {
        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Collection<User> doInBackground(String... params) {
            String query = params[0];
            try {
                return httpClient.readUsers(query);
            } catch (IOException | JSONException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Collection<User> users) {
            swipeRefreshLayout.setRefreshing(false);

            // успешный ответ
            if (users != null) {
                usersAdapter.clearItems();
                usersAdapter.setItems(users);
            }
            // ошибка
            else {
                Toast.makeText(SearchUsersActivity.this, "Error during loading info", Toast.LENGTH_SHORT).show();
            }
        }
    }
}