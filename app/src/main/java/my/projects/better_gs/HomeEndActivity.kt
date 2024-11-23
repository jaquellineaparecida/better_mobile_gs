package my.projects.better_gs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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

        // Inicialização do Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Inicialização dos componentes do layout
        containerEnderecos = findViewById(R.id.containerEnderecos)
        mensagemSemEnderecos = findViewById(R.id.enderecosCadastradosTextView)
        adicionarEnderecoTextView = findViewById(R.id.adicionarEnderecoTextView)

        // Configurar clique no botão de adicionar endereço
        adicionarEnderecoTextView.setOnClickListener {
            val intent = Intent(this, CadastroEnderecoActivity::class.java)
            startActivity(intent)
        }

        // Carregar os endereços ao iniciar a Activity
        carregarEnderecos()
    }

    private fun carregarEnderecos() {
        // Limpar o container antes de recarregar os dados
        containerEnderecos.removeAllViews()

        // Buscar dados do Firestore
        db.collection("TB_ENDERECO").get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                // Exibir mensagem se não houver endereços
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

        // Configurar clique no botão de excluir
        excluirButton.setOnClickListener {
            excluirEndereco(docId)
        }

        // Configurar clique no botão de editar
        editarButton.setOnClickListener {
            val intent = Intent(this, CadastroEnderecoActivity::class.java)
            intent.putExtra("enderecoId", docId)
            startActivity(intent)
        }

        // Adicionar a view ao container
        containerEnderecos.addView(view)
    }

    private fun excluirEndereco(docId: String) {
        // Excluir endereço no Firestore
        db.collection("TB_ENDERECO").document(docId).delete().addOnSuccessListener {
            Toast.makeText(this, "Endereço excluído.", Toast.LENGTH_SHORT).show()
            carregarEnderecos() // Recarregar endereços
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
