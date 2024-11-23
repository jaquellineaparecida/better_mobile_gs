package my.projects.better_gs

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class HomeEndActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var containerEnderecos: LinearLayout
    private lateinit var mensagemSemEnderecos: TextView
    private lateinit var adicionarEnderecoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.end_home)
        supportActionBar?.hide()

        db = FirebaseFirestore.getInstance()

        containerEnderecos = findViewById(R.id.containerEnderecos)
        mensagemSemEnderecos = findViewById(R.id.enderecosCadastradosTextView)
        adicionarEnderecoTextView = findViewById(R.id.adicionarEnderecoTextView)

        adicionarEnderecoTextView.setOnClickListener {
            val intent = Intent(this, CadastroEnderecoActivity::class.java)
            startActivity(intent)
        }

        carregarEnderecos()
    }

    private fun carregarEnderecos() {
        containerEnderecos.removeAllViews()

        db.collection("TB_ENDERECO").get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                mensagemSemEnderecos.visibility = View.VISIBLE
                mensagemSemEnderecos.text = "Nenhum endereço cadastrado"
            } else {
                mensagemSemEnderecos.visibility = View.GONE
                for (document in result) {
                    val endereco = document.toObject(Endereco::class.java)
                    adicionarEnderecoView(endereco, document.id)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao carregar endereços.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun adicionarEnderecoView(endereco: Endereco, docId: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.container_endereco, containerEnderecos, false)

        val tagEndereco = view.findViewById<TextView>(R.id.tagEnderecoTextView)
        val enderecoDetalhes = view.findViewById<TextView>(R.id.enderecoDetalhesTextView)
        val cidadeDetalhes = view.findViewById<TextView>(R.id.cidadeDetalhesTextView)
        val estadoDetalhes = view.findViewById<TextView>(R.id.estadoDetalhesTextView)
        val cepDetalhes = view.findViewById<TextView>(R.id.cepDetalhesTextView)

        val excluirButton = view.findViewById<ImageButton>(R.id.excluirEnderecoButton)
        val editarButton = view.findViewById<ImageButton>(R.id.editarEnderecoButton)

        tagEndereco.text = endereco.tag
        enderecoDetalhes.text = "Endereço: ${endereco.endereco}"
        cidadeDetalhes.text = "Cidade: ${endereco.cidade}"
        estadoDetalhes.text = "Estado: ${endereco.estado}"
        cepDetalhes.text = "CEP: ${endereco.cep}"

        excluirButton.setOnClickListener {
            excluirEndereco(docId)
        }

        editarButton.setOnClickListener {
            mostrarDialogoEdicaoEndereco(endereco, docId)
        }

        containerEnderecos.addView(view)
    }

    private fun mostrarDialogoEdicaoEndereco(endereco: Endereco, enderecoId: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_endereco, null)

        val enderecoEditText = dialogView.findViewById<EditText>(R.id.editEndereco)
        val cidadeEditText = dialogView.findViewById<EditText>(R.id.editCidade)
        val estadoEditText = dialogView.findViewById<EditText>(R.id.editEstado)
        val cepEditText = dialogView.findViewById<EditText>(R.id.editCep)

        enderecoEditText.setText(endereco.endereco)
        cidadeEditText.setText(endereco.cidade)
        estadoEditText.setText(endereco.estado)
        cepEditText.setText(endereco.cep)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Endereço")
        builder.setView(dialogView)

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.setPositiveButton("Salvar") { dialog, _ ->
            val novoEndereco = enderecoEditText.text.toString()
            val novaCidade = cidadeEditText.text.toString()
            val novoEstado = estadoEditText.text.toString()
            val novoCep = cepEditText.text.toString()

            atualizarEndereco(enderecoId, novoEndereco, novaCidade, novoEstado, novoCep)
            dialog.dismiss()
        }

        builder.show()
    }

    private fun atualizarEndereco(enderecoId: String, novoEndereco: String, novaCidade: String, novoEstado: String, novoCep: String) {
        val enderecoAtualizado = hashMapOf<String, Any>(
            "endereco" to novoEndereco,
            "cidade" to novaCidade,
            "estado" to novoEstado,
            "cep" to novoCep
        )

        db.collection("TB_ENDERECO").document(enderecoId)
            .update(enderecoAtualizado as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Endereço atualizado com sucesso.", Toast.LENGTH_SHORT).show()
                carregarEnderecos()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao atualizar o endereço.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun excluirEndereco(docId: String) {
        db.collection("TB_ENDERECO").document(docId).delete().addOnSuccessListener {
            Toast.makeText(this, "Endereço excluído.", Toast.LENGTH_SHORT).show()
            carregarEnderecos()
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao excluir endereço.", Toast.LENGTH_SHORT).show()
        }
    }
}

data class Endereco(
    val tag: String = "",
    val endereco: String = "",
    val cidade: String = "",
    val estado: String = "",
    val cep: String = ""
)
