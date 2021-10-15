package com.kang.instargramapp.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.kang.instargramapp.R
import com.kang.instargramapp.navigation.model.ContentDTO
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

    //유저의 정보를 가져옴
    var auth : FirebaseAuth? = null

    //데이터 베이스를 사용할 수 있도록 가져옴
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        //현재 액티비티를 실행하자마자 화면이 열릴 수 있는 코드
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
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


        //Promise 방식
        storageRef?.putFile(photoUri!!)?.continueWithTask{ task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl}?.addOnSuccessListener { uri ->
            //data model를 만듬
            var contentDTO = ContentDTO()
            //insert image(이미지)
            contentDTO.imageUrl = uri.toString()
            //insert uid
            contentDTO.uid = auth?.currentUser?.uid
            //insert userId
            contentDTO.userId = auth?.currentUser?.email
            //insert content(사용자가 입력한 설명)
            contentDTO.explain = addphoto_edit_explain.text.toString()
            //insert 시간
            contentDTO.timestamp = System.currentTimeMillis()
            //이미지 셋 컬렉션에 DTO 넣기
            firestore?.collection("images")?.document()?.set(contentDTO)
            //정상적으로 닫혔다는 플래그 값인 RESULT_OK값을 넣어준다.
            setResult(Activity.RESULT_OK)
            //창닫기
            finish()
        }

       /* //Callback 방식 이미지 업로드
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            // 이미지 주소를 받아옴
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                //data model를 만듬
                var contentDTO = ContentDTO()
                //insert image(이미지)
                contentDTO.imageUrl = uri.toString()
                //insert uid
                contentDTO.uid = auth?.currentUser?.uid
                //insert userId
                contentDTO.userId = auth?.currentUser?.email
                //insert content(사용자가 입력한 설명)
                contentDTO.explain = addphoto_edit_explain.text.toString()
                //insert 시간
                contentDTO.timestamp = System.currentTimeMillis()
                //이미지 셋 컬렉션에 DTO 넣기
                firestore?.collection("images")?.document()?.set(contentDTO)
                //정상적으로 닫혔다는 플래그 값인 RESULT_OK값을 넣어준다.
                setResult(Activity.RESULT_OK)
                //창닫기
                finish()
            }
        }*/
    }
}