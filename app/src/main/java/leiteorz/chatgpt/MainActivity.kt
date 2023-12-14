package leiteorz.chatgpt

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import leiteorz.chatgpt.model.ChatMessage
import leiteorz.chatgpt.ui.theme.ChatTestTheme
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFAFAFA)
                ) {
                    val systemUiController = rememberSystemUiController()
                    SideEffect {
                        systemUiController.setStatusBarColor(
                            color = Color(0xFF0288D1)
                        )
                    }

                    val messageList = remember { mutableStateListOf<ChatMessage>() }
                    chatApp(messageList = messageList)
                }
            }
        }
    }

    // 合起来
    @Composable
    fun chatApp(messageList: SnapshotStateList<ChatMessage>){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            TopAppBar(
                title = {Text("ChatGPT APP", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)},
                backgroundColor = Color(0xFF0288D1),
                contentColor = Color.White,
                modifier = Modifier
            )
            chatList(messageList = messageList, modifier = Modifier.weight(1f))
            inputBox(messageList = messageList, modifier = Modifier)
        }
    }

    // 聊天列表
    @Composable
    fun chatList(messageList: SnapshotStateList<ChatMessage>, modifier: Modifier){
        LazyColumn(modifier = modifier){
            items(messageList){
                DialogBox(text = it.text, type = it.type)
            }
        }
    }

    // 对话框
    @Composable
    fun DialogBox(text: String, type: Int){
        val identity: String = if(type == 0) "Me" else "ChatGPT"
        val dialogColor: Color = if(type == 0) Color(0xFFE91E63) else Color(0xFF64B5F6)

        Column(
            modifier = Modifier.offset(x = 20.dp, y = 5.dp)
        ) {
            Text(text = identity, color = dialogColor, fontWeight = FontWeight.Bold)
            // 对话框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .offset(y = 2.dp)
            ) {
                Divider(
                    color = dialogColor,
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .padding(0.dp, 8.dp)
                )
                Text(
                    text = text,
                    modifier = Modifier
                        .width(320.dp)
                        .padding(5.dp)
                )
            }
        }
    }



    // 得到回复
    private fun sendQuestion(question: String, messageList: SnapshotStateList<ChatMessage>){
        val url = "https://api.openai.com/v1/completions"
        // 创建一个请求队列
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        // 提问
        val jsonObject = JSONObject()
        jsonObject.put("model", "text-davinci-003")
        jsonObject.put("prompt", question)
        jsonObject.put("temperature", 0)
        jsonObject.put("max_tokens", 300)
        jsonObject.put("top_p", 1)
        jsonObject.put("frequency_penalty", 0.0)
        jsonObject.put("presence_penalty", 0.0)

        val postRequest: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonObject,
                Response.Listener { response ->
                    val responseMsg: String = response.getJSONArray("choices").getJSONObject(0).getString("text")
                    Log.d("reply", "getResponse: " + responseMsg)
                    messageList.add(ChatMessage(responseMsg.trim(), 1))
                },
                Response.ErrorListener { error ->
                    Log.d("OpenAI", "getResponse: " + error.message + "\n" + error)
                }){
                override fun getHeaders(): MutableMap<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Type"] = "application/json"
                    params["Authorization"] = "Bearer sk-xxx"
                    return params
                }
            }

        queue.add(postRequest)
    }

    // 输入框
    @Composable
    fun inputBox(messageList: SnapshotStateList<ChatMessage>, modifier: Modifier){
        var userInput by remember { mutableStateOf("") }    // 用户输入
        val context = LocalContext.current

        TextField(
            value = userInput,
            onValueChange = {userInput = it},
            placeholder = {
                Text(text = "Send a chat")
            },
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color(0xFFEEEEEE),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (userInput.isEmpty()) Toast.makeText(context, "聊天内容不能为空", Toast.LENGTH_SHORT).show()
                else{
                    messageList.add(ChatMessage(userInput, 0))

                    val question = userInput
                    userInput = ""

                    sendQuestion(question, messageList)
                }
            })
        )
    }
}

