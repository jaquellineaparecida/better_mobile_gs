package my.projects.better_gs

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cadastro)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        val nomeCompletoEditText: EditText = findViewById(R.id.nomeCompletoEditText)
        val dataNascimentoEditText: EditText = findViewById(R.id.dataNascimentoEditText)
        val cpfEditText: EditText = findViewById(R.id.cpfEditText)
        val telefoneEditText: EditText = findViewById(R.id.telefoneEditText)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val senhaEditText: EditText = findViewById(R.id.senhaEditText)
        val cadastrarButton: Button = findViewById(R.id.cadastrarButton)

        cadastrarButton.setOnClickListener {
            val nome = nomeCompletoEditText.text.toString().trim()
            val dataNascimento = dataNascimentoEditText.text.toString().trim()
            val cpf = cpfEditText.text.toString().trim()
            val telefone = telefoneEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val senha = senhaEditText.text.toString().trim()

            if (nome.isEmpty() || dataNascimento.isEmpty() || cpf.isEmpty() || telefone.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            salvarDados(userId, nome, dataNascimento, cpf, telefone, email)
                        }
                    } else {
                        Toast.makeText(this, "Erro no cadastro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun salvarDados(userId: String, nome: String, dataNascimento: String, cpf: String, telefone: String, email: String) {
        val firestore = FirebaseFirestore.getInstance()
        val usuario = mapOf(
            "nome" to nome,
            "dataNascimento" to dataNascimento,
            "cpf" to cpf,
            "telefone" to telefone,
            "email" to email
        )

        firestore.collection("TB_USUARIO").document(userId)
            .set(usuario)
            .addOnSuccessListener {
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar dados no Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
