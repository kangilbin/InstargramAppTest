package com.kang.instargramapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        button1.setOnClickListener {
            signinAndSignup()
        }
    }

    fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
            ?.addOnCompleteListener  { task ->
                if (task.isSuccessful) {
                    // id가 생성되었을 때 코드
                    moveMainPage(task.result?.user)
                } else if(task.exception?.message.isNullOrEmpty()) {
                    //로그인 실패시 에러 메세지만 출력
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                } else {
                    //회원 가입도 에러메세지도 아닐 때 로그인 하는 장소로 이동
                    signinEmail()
                }
            }
    }

    //로그인 하는 코드
    fun signinEmail(){
        auth?.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
            ?.addOnCompleteListener  { task ->
                if (task.isSuccessful) {
                    // id와 paswword가 맞았을 때
                    moveMainPage(task.result?.user)
                } else {
                    //틀릴 때
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    //로그인 성공 후 다음 페이지로 이동
    fun moveMainPage(user:FirebaseUser?){
        if(user != null){
            //다음 페이지로 이동하는 Intent 코드 메인 액티비티를 호출하는 코드 넣기
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}