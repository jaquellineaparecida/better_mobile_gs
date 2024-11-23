package my.projects.better_gs

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class CadastroEnderecoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var enderecoEditText: EditText
    private lateinit var cidadeEditText: EditText
    private lateinit var estadoEditText: EditText
    private lateinit var cepEditText: EditText
    private lateinit var tagEditText: EditText
    private lateinit var cadastrarButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.en_cadastro)
        supportActionBar?.hide()

        db = FirebaseFirestore.getInstance()

        enderecoEditText = findViewById(R.id.enderecoEditText)
        cidadeEditText = findViewById(R.id.cidadeEditText)
        estadoEditText = findViewById(R.id.estadoEditText)
        cepEditText = findViewById(R.id.cepEditText)
        tagEditText = findViewById(R.id.tagEditText)
        cadastrarButton = findViewById(R.id.cadastrarButton)

        cadastrarButton.setOnClickListener {
            cadastrarEndereco()
        }
    }

    private fun cadastrarEndereco() {
        val endereco = enderecoEditText.text.toString().trim()
        val cidade = cidadeEditText.text.toString().trim()
        val estado = estadoEditText.text.toString().trim()
        val cep = cepEditText.text.toString().trim()
        val tag = tagEditText.text.toString().trim()

        if (endereco.isEmpty() || cidade.isEmpty() || estado.isEmpty() || cep.isEmpty() || tag.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        val novoEndereco = mapOf(
            "endereco" to endereco,
            "cidade" to cidade,
            "estado" to estado,
            "cep" to cep,
            "tag" to tag
        )

        db.collection("TB_ENDERECO").add(novoEndereco)
            .addOnSuccessListener {
                Toast.makeText(this, "Endereço cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                finish() // Fecha a activity após o cadastro
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao cadastrar endereço.", Toast.LENGTH_SHORT).show()
            }
    }
}
