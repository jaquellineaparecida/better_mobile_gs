package my.projects.better_gs

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore


class HomeActivity : AppCompatActivity() {

    private lateinit var enderecoButton: Button
    private lateinit var estacoesButton: Button
    private lateinit var aluguelButton: Button
    private lateinit var transporteButton: Button
    private lateinit var listaTituloTextView: TextView
    private var endereco: String? = null
    private lateinit var listaEstacoesLayout: LinearLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
        supportActionBar?.hide()

        enderecoButton = findViewById(R.id.enderecoButton)
        estacoesButton = findViewById(R.id.estacoesButton)
        aluguelButton = findViewById(R.id.aluguelButton)
        transporteButton = findViewById(R.id.transporteButton)
        listaTituloTextView = findViewById(R.id.listaTituloTextView)
        listaEstacoesLayout = findViewById(R.id.listaEstacoesLayout)

        enderecoButton.setOnClickListener { mostrarDialogoEnderecos() }
        estacoesButton.setOnClickListener { buscarEstacoes() }
        aluguelButton.setOnClickListener { listarAlugueis() }
        transporteButton.setOnClickListener { listarTransportes() }

        val irParaEnderecosTextView: TextView = findViewById(R.id.irParaEnderecosTextView)
        irParaEnderecosTextView.setOnClickListener {
            val intent = Intent(this, HomeEndActivity::class.java)
            startActivity(intent)
        }

    }

    private fun mostrarDialogoEnderecos() {
        db.collection("TB_ENDERECO")
            .get()
            .addOnSuccessListener { result ->
                val opcoesEnderecos = mutableListOf<String>()
                for (document in result) {
                    val endereco = document.getString("endereco")
                    endereco?.let { opcoesEnderecos.add(it) }
                }
                if (opcoesEnderecos.isEmpty()) {
                    val intent = Intent(this, HomeEndActivity::class.java)
                    startActivity(intent)
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Selecione um endereço")
                    builder.setItems(opcoesEnderecos.toTypedArray()) { _, which ->
                        endereco = opcoesEnderecos[which]
                        Toast.makeText(this, "Endereço selecionado: ${opcoesEnderecos[which]}", Toast.LENGTH_SHORT).show()
                        atualizarEstacoes()
                    }
                    builder.show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao buscar endereços: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun atualizarEstacoes() {
        listaEstacoesLayout.removeAllViews()
        if (endereco == null) {
            listaTituloTextView.text = "Selecione um endereço para visualizar as estações"
            return
        }

        buscarEstacoes()
    }

    private fun buscarEstacoes() {
        db.collection("TB_ESTACOES_RECARGA")
            .get()
            .addOnSuccessListener { result ->
                val estacoes = mutableListOf<Estacao>()
                for (document in result) {
                    val nome = document.getString("nome") ?: "Desconhecido"
                    val distancia = document.getString("distancia") ?: "Distancia não informado"
                    val endereco = document.getString("endereco") ?: "Endereço não informado"
                    val funcionamento = document.getString("funcionamento") ?: "Não especificado"
                    val precos = document.getString("precos") ?: "Não informado"
                    val pagamento = document.getString("pagamento") ?: "Não especificado"

                    estacoes.add(Estacao(nome, distancia, endereco, funcionamento, precos, pagamento))
                }
                if (estacoes.isNotEmpty()) {
                    listaTituloTextView.text = "Estações de recarga"
                    exibirEstacoes(estacoes)
                } else {
                    listaTituloTextView.text = "Nenhuma estação encontrada"
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao buscar estações: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun exibirLista(itens: List<String>) {
        for (item in itens) {
            val view = layoutInflater.inflate(R.layout.item_estacao, listaEstacoesLayout, false)
            val titulo = view.findViewById<TextView>(R.id.estacaoTituloTextView)
            val distancia = view.findViewById<TextView>(R.id.estacaoDistanciaTextView)

            titulo.text = item.split("\n")[0]
            distancia.text = item.split("\n").drop(1).joinToString("\n")

            listaEstacoesLayout.addView(view)
        }
    }

    private fun listarAlugueis() {
        listaEstacoesLayout.removeAllViews()
        listaTituloTextView.text = "Aluguéis disponíveis"

        db.collection("TB_ALUGUEL")
            .get()
            .addOnSuccessListener { result ->
                val alugueis = mutableListOf<String>()
                for (document in result) {
                    val nome = document.getString("nome") ?: "Desconhecido"
                    val endereco = document.getString("endereco") ?: "Endereço não informado"
                    val preco = document.getString("preco") ?: "Preço não informado"
                    alugueis.add("Nome: $nome\nEndereço: $endereco\nPreço: $preco")
                }
                if (alugueis.isEmpty()) {
                    listaTituloTextView.text = "Nenhum aluguel encontrado"
                } else {
                    exibirLista(alugueis)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao buscar aluguéis: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listarTransportes() {
        listaEstacoesLayout.removeAllViews()
        listaTituloTextView.text = "Transportes Públicos disponíveis"

        db.collection("TB_TRANSP_PUBLICO")
            .get()
            .addOnSuccessListener { result ->
                val transportes = mutableListOf<String>()
                for (document in result) {
                    val nome = document.getString("nome") ?: "Desconhecido"
                    val tipo = document.getString("tipo") ?: "Tipo não informado"
                    val linha = document.getString("linha") ?: "Linha não informada"
                    transportes.add("Nome: $nome\nTipo: $tipo\nLinha: $linha")
                }
                if (transportes.isEmpty()) {
                    listaTituloTextView.text = "Nenhum transporte público encontrado"
                } else {
                    exibirLista(transportes)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao buscar transportes públicos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun exibirEstacoes(estacoes: List<Estacao>) {
        for (estacao in estacoes) {
            val view = layoutInflater.inflate(R.layout.item_estacao, listaEstacoesLayout, false)

            val titulo = view.findViewById<TextView>(R.id.estacaoTituloTextView)
            val distancia = view.findViewById<TextView>(R.id.estacaoEnderecoTextView)
            val enderecoText = view.findViewById<TextView>(R.id.estacaoDistanciaTextView)
            val funcionamento = view.findViewById<TextView>(R.id.estacaoFuncionamentoTextView)
            val precos = view.findViewById<TextView>(R.id.estacaoPrecosTextView)
            val pagamento = view.findViewById<TextView>(R.id.estacaoPagamentoTextView)

            titulo.text = estacao.nome
            distancia.text = "Distância: ${estacao.distancia}"
            enderecoText.text = "Endereço: ${estacao.endereco}"
            funcionamento.text = "Funcionamento: ${estacao.funcionamento}"
            precos.text = "Preços: ${estacao.precos}"
            pagamento.text = "Pagamento: ${estacao.pagamento}"

            listaEstacoesLayout.addView(view)

        }
    }

}

data class Estacao(
    val nome: String,
    val distancia: String,
    val endereco: String,
    val funcionamento: String,
    val precos: String,
    val pagamento: String
)