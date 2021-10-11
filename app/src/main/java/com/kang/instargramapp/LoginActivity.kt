package com.kang.instargramapp

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null
    var callbackManager: CallbackManager? = null

    //구글 로그인 시 필요한 리퀘스트 코드
    var GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        button1.setOnClickListener {
            signinAndSignup()
        }
        button2.setOnClickListener {
            facebookLogin()
        }
        button3.setOnClickListener {
            googleLogin()
        }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestIdToken("getString(R.string.default_web_client_id)")
            .requestEmail() //이메일 받아올 수 있게
            .build()        // 다시 닫아 주기
        //위에 설정한 옵션들을 클라이언트에 셋팅
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        //printHashKey()
        callbackManager = CallbackManager.Factory.create()
    }

    fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // id가 생성되었을 때 코드
                    moveMainPage(task.result?.user)
                } else if (task.exception?.message.isNullOrEmpty()) {
                    //로그인 실패시 에러 메세지만 출력
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                } else {
                    //회원 가입도 에러메세지도 아닐 때 로그인 하는 장소로 이동
                    signinEmail()
                }
            }
    }

    //페이스북 로그인
    fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "key:$hashKey=")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }

    }

    //구글 로그인
    fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    //페이스북 로그인
    fun facebookLogin() {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        //Result는 최종적으로 로그인을 설공했을 때 넘어오는 데이터
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    //페이스북 로그인을 성공하면 파이어베이스에 넘기는 코드
                    handleFacebookAccessToken(result?.accessToken)
                }
                override fun onCancel() {
                }
                override fun onError(error: FacebookException?) {
                }
            })
    }
    fun handleFacebookAccessToken(token : AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // id와 paswword가 맞았을 때
                    moveMainPage(task.result?.user)
                } else {
                    //틀릴 때
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            // 구굴에서 넘겨주는 로그인 반환 값
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // 성공했을 때 이 값을 Firebase에 넘겨준다.
                var account = result.signInAccount
                firebaseAuthWithGoogle(account)
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // id와 paswword가 맞았을 때
                    moveMainPage(task.result?.user)
                } else {
                    //틀릴 때
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    //로그인 하는 코드
    fun signinEmail() {
        auth?.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // id와 paswword가 맞았을 때
                    moveMainPage(task.result?.user)
                } else {
                    //틀릴 때
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    //로그인 성공 후 다음 페이지로 이동
    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            //다음 페이지로 이동하는 Intent 코드 메인 액티비티를 호출하는 코드 넣기
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}