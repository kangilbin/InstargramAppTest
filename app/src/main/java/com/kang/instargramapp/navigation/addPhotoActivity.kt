package com.kang.instargramapp.navigation

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.kang.instargramapp.R
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class addPhotoActivity : AppCompatActivity() {
    //리퀘스트 코드
    var PICK_IMAGE_FROM_ALBUM = 0
    //frebase storege
    var storage : FirebaseStorage? = null
    // 이미지  uri담는 변수
    var photoUri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        storage = FirebaseStorage.getInstance()

        //현재 액티비티를 실행하자마자 화면이 열릴 수 있는 코드
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        //사진 등록 이벤트
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }
    //선택한 이미지를 받는 부분
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            //결과 값이 사진을 선택 했을 때
            if(resultCode == RESULT_OK){
                //사진을 선택하게 되면 이미지의 경로가 넘어온다.
                photoUri = data?.data  //경로 담기
                addphoto_image.setImageURI(photoUri) // 선택한 이미지 표시
            } else {
                //취소시 작동
                finish()
            }
        }
    }
    fun contentUpload(){
        //파일명을 만드는 변수 중복되지 않게 날짜 값으로 만든다.
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        // 이미지 업로드 변수  image라는 폴더 안에 파일명으로 생성
        var storageRef = storage?.reference?.child("image")?.child(imageFileName)

        // 이미지 업로드
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            //성공시 메세지
            Toast.makeText(this, getString(R.string.upload_success),Toast.LENGTH_LONG).show()
        }
    }
}