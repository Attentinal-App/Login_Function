package com.kmou.capstonelogin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.common.KakaoSdk;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kakao SDK 초기화 (애플리케이션 컨텍스트를 사용해야 합니다)
        KakaoSdk.init(this, getString(R.string.kakao_app_key));

        // Google Sign-In 옵션 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Google 로그인 버튼 클릭 리스너 설정
        findViewById(R.id.btnGoogleLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        // Kakao 로그인 버튼 클릭 리스너 설정
        findViewById(R.id.btnKakaoLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithKakao();
            }
        });
    }

    // Google 로그인 실행 메서드
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    // Google 로그인 결과 처리
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                try {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    handleGoogleSignInResult(account);
                } catch (ApiException e) {
                    Log.w(TAG, "Google 로그인 실패, 상태 코드: " + e.getStatusCode(), e);
                    Toast.makeText(MainActivity.this, "Google 로그인 실패", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Google 로그인 성공 시 사용자 정보 처리
    private void handleGoogleSignInResult(GoogleSignInAccount account) {
        if (account != null) {
            String email = account.getEmail();
            String displayName = account.getDisplayName();
            Toast.makeText(this, "Google 환영합니다, " + displayName, Toast.LENGTH_SHORT).show();
        }
    }

    // Kakao 로그인 실행 메서드
    private void signInWithKakao() {
        // 카카오톡 설치 여부에 따라 로그인 방식 선택
        if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
            UserApiClient.getInstance().loginWithKakaoTalk(this, kakaoCallback);
        } else {
            UserApiClient.getInstance().loginWithKakaoAccount(this, kakaoCallback);
        }
    }

    // Kakao 로그인 콜백
    private final Function2<OAuthToken, Throwable, Unit> kakaoCallback = new Function2<OAuthToken, Throwable, Unit>() {
        @Override
        public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
            if (oAuthToken != null) {
                // 로그인 성공 시
                Toast.makeText(MainActivity.this, "Kakao 로그인 성공", Toast.LENGTH_SHORT).show();
                fetchKakaoUserInfo();
            }
            if (throwable != null) {
                // 로그인 실패 시
                Log.e(TAG, "Kakao 로그인 실패", throwable);
                Toast.makeText(MainActivity.this, "Kakao 로그인 실패", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    };

    // Kakao 사용자 정보 가져오기
    private void fetchKakaoUserInfo() {
        UserApiClient.getInstance().me((user, throwable) -> {
            if (user != null) {
                String nickname = user.getKakaoAccount().getProfile().getNickname();
                String email = user.getKakaoAccount().getEmail();
                Toast.makeText(this, "환영합니다, " + nickname, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Kakao User Email: " + email);
            }
            return null;
        });
    }

    // Naver 로그인 실행 메서드
    private void startNaverLogin() {
        mOAuthLoginModule.startOauthLoginActivity(MainActivity.this, new OAuthLoginHandler() {
            @Override
            public void run(boolean success) {
                if (success) {
                    String accessToken = mOAuthLoginModule.getAccessToken(mContext);
                    String refreshToken = mOAuthLoginModule.getRefreshToken(mContext);
                    long expiresAt = mOAuthLoginModule.getExpiresAt(mContext);
                    String tokenType = mOAuthLoginModule.getTokenType(mContext);

                    Log.i("NaverLoginData", "accessToken : " + accessToken);
                    Toast.makeText(MainActivity.this, "Naver 로그인 성공", Toast.LENGTH_SHORT).show();
                } else {
                    String errorCode = mOAuthLoginModule.getLastErrorCode(mContext).getCode();
                    String errorDesc = mOAuthLoginModule.getLastErrorDesc(mContext);
                    Toast.makeText(mContext, "Naver 로그인 실패: " + errorCode + ", " + errorDesc, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            handleGoogleSignInResult(account); // Google 자동 로그인
        }
    }
}
