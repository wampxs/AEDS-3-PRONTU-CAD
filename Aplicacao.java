package prontucad;
import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.Math;

/*
 AUTOR: DIOGO BARROS
 
 Cadastro de Prontuários estruturado em Hashing Extensível
 
 */


// Classe para comportar o caminho dos arquivos do sistema.
class DIR{
	static String PATH = System.getProperty("user.dir") + "\\703267\\";
}
// Lápide: Objeto que armazena o estado atual do cliente (Deletado ou não), 
// assim como o próximo cliente deletado (Lista encadeada para indicar os próximos registros deletados a serem sobrescritos)
class Lapide{
	private boolean state;
	private int next;
	public Lapide() {
		this.state=false;
		this.next=0;
	}
	public Lapide(boolean state) { // cria lápide para cliente null
		this.state=state;
		this.next=-1;
	}
	public boolean isState() {
		return state;
	}
	public void setState(boolean state) {
		this.state = state;
	}
	public int getNext() {
		return next;
	}
	public void setNext(int next) {
		this.next = next;
	}
	@Override
	public String toString() {
		return "Lapide [state=" + state + ", next=" + next + "]";
	}
}
// MetaFile: arquivo que armazenará as variáveis de programa definidas para o arquivo atual
// DescTam = Tamanho da descrição, BucketQtd = quantidade de células em um bucket, ProfInicial = profundidade inicial do diretório
class MetaFile{
	private int descTam;
	private int bucketQtd;
	private int profInicial;
	
	public int getDescTam() {
		return descTam;
	}
	public void setDescTam(int descTam) {
		this.descTam = descTam;
	}
	public int getBucketQtd() {
		return bucketQtd;
	}
	public void setBucketQtd(int bucketQtd) {
		this.bucketQtd = bucketQtd;
	}
	public int getProfInicial() {
		return profInicial;
	}
	public void setProfInicial(int profInicial) {
		this.profInicial = profInicial;
	}
	private void setBytes(byte[] array) {
		ByteArrayInputStream buffer = new ByteArrayInputStream(array);
		DataInputStream in = new DataInputStream(buffer);
		try {
			this.descTam = in.readInt();
			this.bucketQtd = in.readInt();
			this.profInicial = in.readInt();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void read() {
		RandomAccessFile file;
		int nbytes = 0;
		byte[] buffer = new byte[12];
		try {
			file = new RandomAccessFile(DIR.PATH+"metadata.txt","rw");
			file.seek(0);
			nbytes = file.read(buffer);
			if(nbytes>0) {
				this.setBytes(buffer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public byte[] getBytes() {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buffer);
		try {
			out.writeInt(this.descTam);
			out.writeInt(this.bucketQtd);
			out.writeInt(this.profInicial);
			out.flush();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}
	public void write() {
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(DIR.PATH+"metadata.txt","rw");
			file.seek(0);
			file.write(this.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public MetaFile() {
		try {
			RandomAccessFile metafile = new RandomAccessFile(DIR.PATH+"metadata.txt","rw");
			if(metafile.length()>0) {
				this.read();
			} else {
				this.descTam = 60;
				this.bucketQtd = 4;
				this.profInicial = 1;
			}
			metafile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
// CabecalhoCliente: Classe para o cabeçalho do arquivo principal.
// qtd = Quantidade de clientes, qtdAlloc = Quantidade de clientes alocados (inclui deletados), 
// descTam = tamanho da descrição, nextDel = próximo registro deletado a ser sobrescrito (se == 0, não há)
// lastDel = último registro que foi deletado
class CabecalhoCliente{
	private int qtd;
	private int qtdAlloc;
	private int descTam;
	private int nextDel;
	private int lastDel;
	private static final int CABECALHO_TAM = 4*5;
	
	private void setBytes(byte[] array) {
		ByteArrayInputStream buffer = new ByteArrayInputStream(array);
		DataInputStream in = new DataInputStream(buffer);
		try {
			this.qtd = in.readInt();
			this.qtdAlloc = in.readInt();
			this.descTam = in.readInt();
			this.nextDel = in.readInt();
			this.lastDel = in.readInt();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void read(RandomAccessFile file) {
		int nbytes = 0;
		byte[] buffer = new byte[CABECALHO_TAM];
		try {
			file.seek(0);
			nbytes = file.read(buffer);
			if(nbytes>0) {
				this.setBytes(buffer);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void read(String fileS) {
		try {
			RandomAccessFile file = new RandomAccessFile(fileS,"rw");
			this.read(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public CabecalhoCliente() {
		this.qtd = 0;
		this.qtdAlloc = 0;
		this.descTam = 0;
		this.nextDel = 0;
		this.lastDel = 0;
	}
	public CabecalhoCliente(String file) {
		read(file);
	}
	public CabecalhoCliente(RandomAccessFile file) {
		read(file);
	}
	public byte[] getBytes() {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buffer);
		try {
			out.writeInt(this.qtd);
			out.writeInt(this.qtdAlloc);
			out.writeInt(this.descTam);
			out.writeInt(this.nextDel);
			out.writeInt(this.lastDel);
			out.flush();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}
	public void write(RandomAccessFile file) {
		try {
			file.seek(0);
			file.write(this.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void write(String fileS) {
		
		try {
			RandomAccessFile file = new RandomAccessFile(fileS,"rw");
			this.write(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public int getQtd() {
		return qtd;
	}
	public void setQtd(int qtd) {
		this.qtd = qtd;
	}
	public int getQtdAlloc() {
		return qtdAlloc;
	}
	public void setQtdAlloc(int qtdAlloc) {
		this.qtdAlloc = qtdAlloc;
	}
	public int getDescTam() {
		return descTam;
	}
	public void setDescTam(int descTam) {
		this.descTam = descTam;
	}
	public int getNextDel() {
		return nextDel;
	}
	public void setNextDel(int nextDel) {
		this.nextDel = nextDel;
	}
	public int getLastDel() {
		return lastDel;
	}
	public void setLastDel(int lastDel) {
		this.lastDel = lastDel;
	}
	@Override
	public String toString() {
		return "CabecalhoCliente [qtd=" + qtd + ", qtdAlloc=" + qtdAlloc + ", descTam=" + descTam + ", nextDel="
				+ nextDel + ", lastDel=" + lastDel + "]";
	}
}
// Cliente: Classe representando o cliente, com todos os dados relevantes.
// "Nome" possui um limite de 24 caracteres.
class Cliente{
	private int cpf;
	private String nome;
	private String nascimento;
	private char sexo;
	private String descricao;
	private Lapide lapide;
	private static final int NOME_TAM = 24;
	private static final int DATA_TAM = 10;
	private int DESC_TAM;
	private int CLIENTE_TAM;
	//CLIENTE_TAM = int+nomeUTF+dataUTF+char+descUTF
	//4+(20+2)+(10+2)+2+(60+2)=102
	public Cliente(int cpf, String nome, String nascimento, char sexo, String descricao) {
		super();
		MetaFile meta = new MetaFile();
		DESC_TAM = meta.getDescTam();
		CLIENTE_TAM = 4+(NOME_TAM*2)+2+(DATA_TAM*2)+2+2+(DESC_TAM*2)+2+5; // int+string+string+char+string+Lapide
		this.cpf = cpf;
		try {
			if(nome.getBytes("UTF-8").length>NOME_TAM) {
				byte[] temp = Arrays.copyOfRange(nome.getBytes("UTF-8"), 0, NOME_TAM-2);
				this.nome = new String(temp);
			} else {
				this.nome = nome;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(nascimento.length()>DATA_TAM) {
			nascimento=nascimento.substring(0,9);
		}
		this.nascimento = nascimento;
		this.sexo = sexo;
		try {
			if(descricao.getBytes("UTF-8").length>DESC_TAM) {
				byte[] temp = Arrays.copyOfRange(descricao.getBytes("UTF-8"), 0, DESC_TAM-1);
				this.descricao = new String(temp);
			} else {
				this.descricao = descricao;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.lapide = new Lapide();
	}
	public Cliente() {
		super();
		MetaFile meta = new MetaFile();
		DESC_TAM = meta.getDescTam();
		CLIENTE_TAM = 4+(NOME_TAM*2)+2+(DATA_TAM*2)+2+2+(DESC_TAM*2)+2+5; // int+string+string+char+string+Lapide
		this.cpf = -1;
		this.nome = "";
		this.nascimento = "";
		this.sexo = ' ';
		this.descricao = "";
		this.lapide = new Lapide(true);
	}

	public int getCpf() {
		return cpf;
	}

	public void setCpf(int cpf) {
		this.cpf = cpf;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		try {
			if(nome.getBytes("UTF-8").length>NOME_TAM) {
				byte[] temp = Arrays.copyOfRange(nome.getBytes("UTF-8"), 0, 19);
				this.nome = new String(temp);
			} else {
				this.nome = nome;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getNascimento() {
		return nascimento;
	}

	public void setNascimento(String nascimento) {
		if(nascimento.length()>DATA_TAM) {
			nascimento=nascimento.substring(0,9);
		}
		this.nascimento = nascimento;
	}

	public char getSexo() {
		return sexo;
	}

	public void setSexo(char sexo) {
		this.sexo = sexo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		try {
			if(descricao.getBytes("UTF-8").length>DESC_TAM) {
				byte[] temp = Arrays.copyOfRange(descricao.getBytes("UTF-8"), 0, 58);
				this.descricao = new String(temp);
			} else {
				this.descricao = descricao;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public Lapide getLapide() {
		return lapide;
	}
	public void setLapide(Lapide lapide) {
		this.lapide = lapide;
	}
	public void setDeleted(boolean deleted) {
		this.lapide.setState(deleted);
	}
	public boolean getDeleted() {
		return this.lapide.isState();
	}
	@Override
	public String toString() {
		return "Cliente [cpf=" + cpf + ", nome=" + nome + ", nascimento=" + nascimento + ", sexo=" + sexo
				+ ", descricao=" + descricao + ", lapide=" + lapide.toString() + "]";
	}
	public byte[] getBytes(){
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buffer);
		try {
			out.writeInt(this.cpf);
			out.writeUTF(this.nome);
			out.writeUTF(this.nascimento);
			out.writeChar(this.sexo);
			out.writeUTF(this.descricao);
			out.writeBoolean(this.lapide.isState());
			out.writeInt(this.lapide.getNext());
			out.flush();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}
	public void setBytes(byte[] array) {
		ByteArrayInputStream buffer = new ByteArrayInputStream(array);
		DataInputStream in = new DataInputStream(buffer);
		try {
			this.cpf = in.readInt();
			this.nome = in.readUTF();
			this.nascimento = in.readUTF();
			this.sexo = in.readChar();
			this.descricao = in.readUTF();
			this.lapide.setState(in.readBoolean());
			this.lapide.setNext(in.readInt());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void write(RandomAccessFile file, long pos) {
		try {
			file.seek(pos*CLIENTE_TAM);
			file.write(this.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void read(RandomAccessFile file, long pos) {
		int nbytes = 0;
		try {
			byte[] buffer = new byte[CLIENTE_TAM];
			file.seek(pos*CLIENTE_TAM);
			nbytes = file.read(buffer);
			if(nbytes>0) {
				this.setBytes(buffer);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
// CRUDCliente : Classe para manipular o arquivo mestre (CRUD para Clientes)
class CRUDCliente{
	// ler na posição
	public Cliente read(RandomAccessFile file, int pos) {
		try {
			file.seek(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		CabecalhoCliente cabecalho = new CabecalhoCliente(file);
		Cliente temp = new Cliente();
		int qtdAlloc = cabecalho.getQtdAlloc();
		if(pos==0 ||pos > qtdAlloc) { // Se posição == 0 ou maior que a quantidade alocada
			System.out.println("(READ) POSIÇÃO "+ pos +" INVÁLIDA!");
			temp = null;//
		} else {
			temp.read(file, pos);
			if(temp.getDeleted()==true) { // Se registro está deletado
				System.out.println("REGISTRO "+pos+" INVÁLIDO(CPF="+temp.toString()+")");
				temp = null;//
			}
		}
		return temp;
	}
	// ler na posição (resgata o cliente mesmo se deletado)
	public Cliente readLapide(RandomAccessFile file, int pos) {
		try {
			file.seek(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		CabecalhoCliente cabecalho = new CabecalhoCliente(file);
		Cliente temp = new Cliente();
		int qtd = cabecalho.getQtd();
		int qtdAlloc = cabecalho.getQtdAlloc();
		if(pos==0 || pos > qtdAlloc) {
			System.out.println("(READ) POSIÇÃO "+ pos +" INVÁLIDA! (LAPIDE) (qtd = "+qtd+")");
		} else {
			temp.read(file, pos);
		}
		return temp;
	}
	// resgata a próxima posição a ser inserida
	public int getNextPos(RandomAccessFile file) {
		CabecalhoCliente cabecalho = new CabecalhoCliente(file);
		int pos;
		if(cabecalho.getNextDel()>0 && cabecalho.getNextDel()<=cabecalho.getQtdAlloc()) { // se há nextDel, retorna a posição
			pos = cabecalho.getNextDel();
		} else { // se não, retorna qtdAlloc + 1
			pos = cabecalho.getQtdAlloc()+1;
		}
		return pos;
	}
	// cria o cliente fornecido
	public void create(Cliente cli, RandomAccessFile file) {
		cli.setDeleted(false);
		CabecalhoCliente cabecalho = new CabecalhoCliente(file);
		cabecalho.setQtd(cabecalho.getQtd()+1); // aumenta qtd no cabeçalho
		int nextdel = cabecalho.getNextDel();
		int pos;
		if(nextdel>0 && nextdel<=cabecalho.getQtdAlloc()){ //se houver registro deletado e o registro estiver no alcance do arquivo atualmente...
			pos = cabecalho.getNextDel(); //posição a ser escrita = posição do registro deletado
			Lapide thisLapide = readLapide(file,cabecalho.getNextDel()).getLapide(); // resgata a lápide do registro deletado
			cabecalho.setNextDel(thisLapide.getNext()); //define posição do próximo registro deletado a ser sobrescrito
		} else { // se não...
			cabecalho.setQtdAlloc(cabecalho.getQtdAlloc()+1); // aumenta qtdAlloc
			pos = cabecalho.getQtdAlloc(); // pos = última nova posição
		}
		cli.write(file, pos); // escreve cliente no arquivo
		if(cabecalho.getNextDel()!=0) { // se ainda há um valor nextDel
			Lapide verificaLapide = readLapide(file,cabecalho.getNextDel()).getLapide(); // resgata a posição desse valor
			if(verificaLapide.isState() == false) { // se o cliente inserido substituiu esse registro nextDel...
				cabecalho.setNextDel(0); // zera nextDel
				cabecalho.setLastDel(0); // zera lastDel
			}
		}
		cabecalho.write(file); // atualiza o cabeçalho no arquivo
	}
	// lê o arquivo mestre
	public void readAll(RandomAccessFile file) {
		CabecalhoCliente cabecalho = new CabecalhoCliente(file);
		int qtd = cabecalho.getQtdAlloc();
		Cliente temp = new Cliente();
		for(int i=0;i<qtd;i++) {
			temp = this.read(file,i+1);
			if(temp!=null) {
				System.out.println(temp.toString());
			}
		}
	}
	// lê o arquivo mestre (inclusive deletados)
	public void readAllLapide(RandomAccessFile file) {
		CabecalhoCliente cabecalho = new CabecalhoCliente(file);
		int qtd = cabecalho.getQtdAlloc();
		Cliente temp = new Cliente();
		for(int i=0;i<qtd;i++) {
			temp = this.readLapide(file,i+1);
			if(temp!=null) {
				System.out.println(temp.toString());
			}
		}
	}
	// atualiza cliente na posição para o cliente fornecido
	public void update(RandomAccessFile file, int pos, Cliente cli) {
		CabecalhoCliente cabecalho = new CabecalhoCliente(file);
		int qtd = cabecalho.getQtdAlloc();
		if(pos == 0 || pos>qtd) {
			System.out.println("(UPDATE) POSIÇÃO "+ pos +" INVÁLIDA!");
		} else { 
			cli.write(file, pos);
		}
	}
	// atualiza cliente na posição, mesmo se estiver deletado
	public void updateLapide(RandomAccessFile file, int pos, Cliente cli) {
		CabecalhoCliente cabecalho = new CabecalhoCliente(file);
		int qtd = cabecalho.getQtdAlloc();
		if(pos == 0 || pos>qtd) {
			System.out.println("(UPDATE) POSIÇÃO "+ pos +" INVÁLIDA!");
		} else { 
			cli.write(file, pos);
		}
	}
	// deleta o cliente na posição
	public void delete(RandomAccessFile file, int pos) {
		CabecalhoCliente cabecalho = new CabecalhoCliente(file); 	// Cria novo cabeçalho
		int qtd = cabecalho.getQtd();								// Obtém a quantidade de itens no arquivo
		Cliente temp = new Cliente();								// Cria o cliente temporário
		temp.read(file, pos);										// Obtém o cliente na posição fornecida
		temp.setDeleted(true);										// Define a lápide do cliente como positiva (deletado)
		temp.getLapide().setNext(0);								// Define o próximo item deletado da lápide como 0 (último item)
		try{
			file.seek(0);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(cabecalho.getNextDel()!=0) {								// Se há itens excluídos no arquivo...
			int nextDel=cabecalho.getNextDel();						// Obtém a posição do item
			int lastDel=cabecalho.getLastDel();
			Cliente nextCliLapide;									// 
			nextCliLapide = this.readLapide(file, lastDel);			// Lê o último registro deletado
			Lapide thisLapide = nextCliLapide.getLapide();			// Obtém a lápide do registro
			nextCliLapide.getLapide().setNext(pos);					// Altera o próximo na lápide para o registro sendo deletado agora
			updateLapide(file,lastDel,nextCliLapide);				// Atualiza o penúltimo registro deletado
		} else {													// Se não há itens excluídos...
			cabecalho.setNextDel(pos);								// Define a posição atual como o primeiro a ser substituído
		}
		if(cabecalho.getQtd()>0) {									// Se qtd>0
			cabecalho.setQtd(qtd-1);								// qtd--
		}
		cabecalho.setLastDel(pos);									// Atualiza a posição do último registro deletado
		update(file, pos, temp);									// Atualiza registro (agora deletado)
		cabecalho.write(file);										// Atualiza cabeçalho
	}
}
// Celula: classe que representa um cliente nos Buckets (cpf e posição no arquivo mestre)
class Celula{
	private int cpf;
	private int pos;
	public Celula() {
		this.cpf=0;
		this.pos=0;
	}
	public Celula(int cpf,int pos) {
		this.cpf = cpf;
		this.pos = pos;
	}
	public int getCpf() {
		return cpf;
	}
	public void setCpf(int cpf) {
		this.cpf = cpf;
	}
	public int getPos() {
		return pos;
	}
	public void setPos(int pos) {
		this.pos = pos;
	}
	@Override
	public String toString() {
		return "Celula [cpf=" + cpf + ", pos=" + pos + "]";
	}		
}
// Bucket: classe para representar os buckets do hashing extensível (Índice)
class Bucket{
	private Celula[] celulas;
	private int profLocal;
	private int qtd;
	private int BUCKET_TAM;
	private int BUCKET_QTD;
	public Bucket() {
		MetaFile meta = new MetaFile();
		BUCKET_QTD = meta.getBucketQtd();
		BUCKET_TAM = 4+4+(BUCKET_QTD*2*4); // int + int + Celula (int+int*qtd)
		this.celulas = new Celula[BUCKET_QTD];
		for(int i=0;i<BUCKET_QTD;i++) {
			this.celulas[i] = new Celula(-1,-1);
		}
		this.profLocal = 1;
		this.qtd = 0;
	}
	// construtor que recebe uma profundidade inicial
	public Bucket(int profLocal) {
		MetaFile meta = new MetaFile();
		BUCKET_QTD = meta.getBucketQtd();
		BUCKET_TAM = 4+4+(BUCKET_QTD*2*4);
		this.celulas = new Celula[BUCKET_QTD];
		for(int i=0;i<BUCKET_QTD;i++) {
			this.celulas[i] = new Celula(-1,-1);
		}
		this.profLocal = profLocal;
		this.qtd = 0;
	}
	public Celula[] getCelulas() {
		return celulas;
	}
	public void setCelulas(Celula[] celulas) {
		this.celulas = celulas;
	}
	// adiciona a célula fornecida no bucket
	public boolean addCelula(Celula celula) {
		int posToAdd = qtd;
		boolean falha = false;
		boolean continueLoop = true;
		if(qtd<BUCKET_QTD) { // se ainda cabem células...
			for(int i=0;continueLoop && i<this.getQtd();i++) { // enquanto não encontrou um espaço livre..
				if (this.getCelulas()[i].getPos() <= 0) {
					posToAdd = i; // define a posição
					continueLoop = false;
				}
			}
			this.celulas[posToAdd] = celula; // adiciona célula
			this.qtd++;
		} else {
			falha = true;
			//System.out.println("Não foi possível adicionar Célula " + celula.toString() + " (array cheio!)");
		}
		return falha;
	}
	public int getProfLocal() {
		return profLocal;
	}
	public void setProfLocal(int profLocal) {
		this.profLocal = profLocal;
	}
	public int getQtd() {
		return qtd;
	}
	public void setQtd(int qtd) {
		this.qtd = qtd;
	}
	public int getBUCKET_QTD() {
		return BUCKET_QTD;
	}
	public void setBUCKET_QTD(int bUCKET_QTD) {
		BUCKET_QTD = bUCKET_QTD;
	}
	@Override
	public String toString() {
		return "Bucket [profLocal=" + profLocal + ", qtd=" + qtd + ", celulas=" + Arrays.toString(celulas) + "]";
	}
	public byte[] getBytes(){
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buffer);
		try {
			out.writeInt(this.qtd);
			out.writeInt(this.profLocal);
			for(int i=0;i<BUCKET_QTD;i++) {
				out.writeInt(this.celulas[i].getCpf());
				out.writeInt(this.celulas[i].getPos());
			}
			out.flush();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}
	public void setBytes(byte[] array) {
		ByteArrayInputStream buffer = new ByteArrayInputStream(array);
		DataInputStream in = new DataInputStream(buffer);
		try {
			this.qtd = in.readInt();
			this.profLocal = in.readInt();
			for(int i=0;i<BUCKET_QTD;i++) {
				//System.out.println("SETBYTES i: "+i+", BUCKET QTD: "+BUCKET_QTD);	
				this.celulas[i].setCpf(in.readInt());
				this.celulas[i].setPos(in.readInt());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void write(RandomAccessFile file, long pos) {
		try {
			file.seek((pos*BUCKET_TAM)+4);
			file.write(this.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void read(RandomAccessFile file, long pos) {
		int nbytes = 0;
		try {
			byte[] buffer = new byte[BUCKET_TAM];
			file.seek((pos*BUCKET_TAM)+4);
			//System.out.println(BUCKET_TAM+" --- "+((pos*BUCKET_TAM)+4));
			nbytes = file.read(buffer);
			if(nbytes>0) {
				this.setBytes(buffer);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void clear() {
		for(int i=0;i<BUCKET_QTD;i++) {
			this.celulas[i] = new Celula(-1,-1);
		}
		this.qtd = 0;
	}
}
// CRUDIndice : classe para manipular o arquivo de índice (CRUD)
class CRUDIndice{
	private int BUCKET_TAM;
	public CRUDIndice() {
		MetaFile meta = new MetaFile();
		int BUCKET_QTD = meta.getBucketQtd();
		BUCKET_TAM = 4+4+(BUCKET_QTD*2*4); // int + int + Celula (int+int *qtd)
	}
	// resgata a quantidade de buckets no arquivo
	public int getQtd(RandomAccessFile file) {
		int nbytes = 0;
		int qtd;
		byte[] array = new byte[BUCKET_TAM];
		ByteArrayInputStream buffer = new ByteArrayInputStream(array);
		DataInputStream in = new DataInputStream(buffer);
		try {
			file.seek(0);
			nbytes = file.read(array);
			if(nbytes>0) {
				qtd = in.readInt();
			} else {
				qtd = 0;
			}
		} catch(Exception e) {
			e.printStackTrace();
			qtd = -1;
		}
		return qtd;
	}
	// cria novo bucket
	public void create(Bucket bucket, RandomAccessFile file) {
		try {
			int qtd = this.getQtd(file);
			qtd++;
			//System.out.println("CREATE QTD: "+qtd);
			file.seek(0);
			file.writeInt(qtd); // atualiza quantidade no arquivo
			bucket.write(file, qtd-1); // insere bucket no fim do arquivo
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// cria novo bucket na posição fornecida
	public void create(Bucket bucket, RandomAccessFile file, int pos) {
		try {
			int qtd = this.getQtd(file);
			//System.out.println("CREATE QTD: "+qtd);
			qtd++;
			file.seek(0);
			file.writeInt(qtd);
			bucket.write(file, pos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// lê o bucket na posição
	public Bucket read(RandomAccessFile file, int pos) {
		Bucket bucket = new Bucket();
		try {
			int qtd = this.getQtd(file);
			if(pos<qtd) { // se posição existir...
				bucket.read(file, pos);
			} else {
				System.out.println("(B)POSIÇÃO " + pos + " INVÁLIDA!");
				bucket = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			bucket = null;
		}
		return bucket;
	}
	// atualiza bucket na posição para o fornecido
	public void update(Bucket bucket, RandomAccessFile file, int pos) {
		try {
			int qtd = this.getQtd(file);
			if(pos<qtd) {
				bucket.write(file, pos);
			} else {
				System.out.println("POSIÇÃO " + pos + " INVÁLIDA!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// deleta bucket na posição
	public void delete(RandomAccessFile file, int pos) {
		Bucket bucket = new Bucket();
		try {
			int qtd = this.getQtd(file);
			if(pos<qtd) {
				bucket.write(file, pos);
				qtd--;
				file.seek(0);
				file.writeInt(qtd);
			} else {
				System.out.println("POSIÇÃO " + pos + " INVÁLIDA!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// deleta uma célula específica dentro de um bucket fornecido
	public void deleteCell(RandomAccessFile file, int bucketPos, int cellPos) {
		Bucket bucket = new Bucket();
		//System.out.println(bucket.toString());
		try {
			int qtd = this.getQtd(file);
			if(bucketPos<qtd) { // se posição do bucket existir...
				bucket.read(file, bucketPos);
				if(cellPos<=bucket.getBUCKET_QTD()) { // se posição da célula existir...
					if(bucket.getCelulas()[cellPos].getPos() >= 0) { // se a célula da posição não já estiver deletada...
						bucket.getCelulas()[cellPos] = new Celula();
						bucket.setQtd(bucket.getQtd()-1);
						this.update(bucket, file, bucketPos);
					} else {
						System.out.println("POSIÇÃO (CELULA) " + cellPos + " JÁ ESTÁ DELETADA!");
					}
				} else {
					System.out.println("POSIÇÃO (CELULA) " + cellPos + " INVÁLIDA!");
				}
			} else {
				System.out.println("POSIÇÃO (BUCKET) " + bucketPos + " INVÁLIDA!");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	// lê todos os buckets do arquivo
	public void readAll(RandomAccessFile file) {
		int qtd = this.getQtd(file);
		//System.out.println("--BQTD: "+qtd);
		Bucket temp = new Bucket();
		for(int i=0;i<qtd;i++) {
			temp = this.read(file,i);
			if(temp!=null) {
				System.out.println("("+i+") "+temp.toString());
			}
		}
	}
}
// Diretorio : classe que representa o diretório para o hashing extensível
class Diretorio{
	private int qtd;
	private int profGlobal;
	private int[] values;
	public int getQtd() {
		return qtd;
	}
	public void setQtd(int qtd) {
		this.qtd = qtd;
	}
	public int getProfGlobal() {
		return profGlobal;
	}
	public void setProfGlobal(int profGlobal) {
		this.profGlobal = profGlobal;
	}
	public int[] getValues() {
		return values;
	}
	public void setValues(int[] values) {
		this.values = values;
	}
	@Override
	public String toString() {
		return "Diretorio [qtd=" + qtd + ", profGlobal=" + profGlobal + ", values=" + Arrays.toString(values) + "]";
	}
	public byte[] getBytes(){
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buffer);
		try {
			out.writeInt(this.qtd);
			out.writeInt(this.profGlobal);
			for(int i=0;i<this.values.length;i++) {
				out.writeInt(this.values[i]);
			}
			out.flush();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}
	public void setBytes(byte[] array) { //atenção
		ByteArrayInputStream buffer = new ByteArrayInputStream(array);
		DataInputStream in = new DataInputStream(buffer);
		try {
			this.qtd = in.readInt();
			this.profGlobal = in.readInt();
			int newArray[] = new int[this.qtd];
			for(int i=0;i<this.qtd;i++) {
				newArray[i] = in.readInt();
			}
			this.values = newArray; 
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void write(RandomAccessFile file) {
		try {
			file.seek(0);
			file.write(this.getBytes());
			//System.out.println("Escrito "+this.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void read(RandomAccessFile file) {
		int nbytes = 0;
		try {
			byte[] buffer = new byte[(int)file.length()];
			file.seek(0);
			nbytes = file.read(buffer);
			if(nbytes>0) {
				this.setBytes(buffer);
				//System.out.println("Lido "+this.toString());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public Diretorio() {
		MetaFile meta = new MetaFile();
		this.profGlobal = meta.getProfInicial();
		this.qtd = (int) Math.pow(2,this.profGlobal); // qtd = 2^profGlobal
		this.values = new int[qtd];
		for(int i=0;i<qtd;i++) {
			this.values[i]=i;
		}
	}
	public Diretorio(int profundidade) {
		this.profGlobal = profundidade;
		this.qtd = (int) Math.pow(2,this.profGlobal);
		this.values = new int[qtd];
		for(int i=0;i<qtd;i++) {
			this.values[i]=i;
		}
	}
	// função para anexar um array do diretório no fim de um anterior (para fins de duplicação)
	public int[] arrayAppend(int[] newArray, int[] oldArray) {
		for(int i=0,j=oldArray.length;i<oldArray.length&&j<newArray.length;i++,j++) {
			newArray[j] = oldArray[i];
		}
		return newArray;
	}
	// duplica o diretório
	public void duplicate() {
		int newLen = this.values.length*2;
		int[] newArray = new int[newLen]; // cria array com o dobro do tamanho
		newArray = Arrays.copyOf(this.values,newLen); // copia o array para o novo array
		this.values = arrayAppend(newArray,this.values); // anexa uma cópia do array no fim do array novo
	}
	// atualiza os buckets aos quais o diretório endereça, 
	// de tal forma a atualizar a posição do bucket novo na SEGUNDA METADE do diretório duplicado
	public void updateBuckets(int targetDir, int newDir) {
		//System.out.println("--ATUALIZANDO VALORES (BUCKET "+targetDir+")--");
		//System.out.println(this.toString());
		int[] newArray = this.getValues();
		boolean changeThis = false;
		for(int i=0;i<newArray.length;i++) { 			// percorre o diretório
			if(newArray[i] == targetDir) { 				// se encontrou o valor a ser substituído
				if(changeThis) { 						// se é uma incidência par desse valor
					newArray[i] = newDir; 					// atualiza o valor
					changeThis = false; 					// impede a próxima alteração
				} else { 								// se for uma incidência ímpar do valor...
					changeThis = true; 						// libera a próxima alteração
				}
			}
		}
		this.setValues(newArray); // atualiza valores do diretório
		//System.out.println("--VALORES ATUALIZADOS--");
		//System.out.println(this.toString());
	}
	// aumenta a profundidade global (efetua um split do bucket em overflow)
	public void increaseProfGlobal(int targetDir, int newDir) {
		//System.out.println("--COMEÇANDO SPLIT (BUCKET "+targetDir+")--");
		//System.out.println(this.toString());
		this.profGlobal += 1; 					// aumenta profundidade em 1
		this.qtd *= 2; 							// duplica a qtd de endereços no diretório
		this.duplicate(); 						// duplica o vetor do diretório
		this.updateBuckets(targetDir, newDir); 	// substitui os valores dos buckets envolvidos no split
	}
}
// CRUDAplicacao: CRUD final que irá unir todos os sistemas e aplicar o hashing extensível em funcionamento.
class CRUDAplicacao{
	RandomAccessFile main; 	// arquivo principal
	RandomAccessFile ind; 	// arquivo de índices
	RandomAccessFile dir; 	// arquivo do diretório
	MetaFile meta;			// arquivo de metadados
	Diretorio diretorio;	// diretório em memória principal
	private int descTam;
	private int bucketQtd;
	private int profInicial;
	public RandomAccessFile getMain() {
		return main;
	}
	public void setMain(RandomAccessFile main) {
		this.main = main;
	}
	public int getMainQTD() {
		CabecalhoCliente cab = new CabecalhoCliente(this.main);
		return cab.getQtd();
		
	}
	public RandomAccessFile getInd() {
		return ind;
	}
	public void setInd(RandomAccessFile ind) {
		this.ind = ind;
	}
	public RandomAccessFile getDir() {
		return dir;
	}
	public void setDir(RandomAccessFile dir) {
		this.dir = dir;
	}
	// cria os arquivos com base em um nome fornecido
	public void criarArquivos(String file) throws Exception {
		Scanner sc = new Scanner(System.in);
		this.main = new RandomAccessFile(DIR.PATH+file+"Main.txt","rw");
		this.ind = new RandomAccessFile(DIR.PATH+file+"Ind.txt","rw");
		this.dir = new RandomAccessFile(DIR.PATH+file+"Dir.txt","rw");
		RandomAccessFile metafile = new RandomAccessFile(DIR.PATH+"metadata.txt","rw");
		meta = new MetaFile();
		if(metafile.length()<=0 && (main.length()<=0 || ind.length()<=0 || dir.length()<=0)) { // se os arquivos não existirem
			System.out.println("ARQUIVOS VAZIOS, CRIANDO ARQUIVOS...");
			System.out.println("Digite o tamanho para as anotações do médico (DEFAULT = 60, MAX = 1000): ");
			descTam = sc.nextInt();
			if(descTam <= 0 || descTam > 1000)
				descTam = 60;
			meta.setDescTam(descTam);
			System.out.println("Digite o tamanho para os buckets (DEFAULT = 4, MAX = 10000): ");
			bucketQtd = sc.nextInt();
			if(bucketQtd <= 0 || bucketQtd > 10000)
				bucketQtd = 4;
			meta.setBucketQtd(bucketQtd);
			System.out.println("Digite a profundidade global inicial (DEFAULT = 1, MAX = 32): ");
			profInicial = sc.nextInt();
			if(profInicial <= 0 || profInicial > 32)
				profInicial = 1;
			meta.setProfInicial(profInicial);
			meta.write();
			new Diretorio(profInicial).write(dir);
		} else { // se existirem, ler os metadados
			meta.read();
			descTam = meta.getDescTam();
			bucketQtd = meta.getBucketQtd();
			profInicial = meta.getProfInicial();
		}
	}
	public CRUDAplicacao(String file) {
		try {
			criarArquivos(file);
			diretorio = new Diretorio();
			diretorio.read(dir);
			if(ind.length()==0) { // se arquivo de índice não existir
				CRUDIndice crudInd = new CRUDIndice();
				Bucket bucket = new Bucket(diretorio.getProfGlobal()); // cria bucket vazio
				for(int i=0;i<diretorio.getQtd();i++) {
					crudInd.create(bucket, ind); // preenche o índice com buckets vazios conforme a quantidade endereçada pelo diretório
				}
				//System.out.println("QTD: "+crudInd.getQtd(ind));
				//crudInd.readAll(ind);
				//System.out.println("FIM DE CRIAÇÃO DE CRUD");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// obtém a posição no índice através do diretório, usando a função hash (cpf % qtd dir)
	public int dirGetIndice(int cpf) {
		return diretorio.getValues()[cpf % diretorio.getQtd()];
	}
	// função para incluir a célula que causou overflow no novo vetor do bucket pós-split
	public Celula[] appendCelula(Celula[] thisCelulas,Celula newCelula) {
		Celula[] newCelulas = new Celula[thisCelulas.length+1];
		for(int i=0;i<thisCelulas.length;i++) {
			newCelulas[i] = thisCelulas[i];
		}
		newCelulas[newCelulas.length-1]=newCelula;
		return newCelulas;
	}
	
	/* 	splitBuckets(): Realiza a divisão dos buckets no índice. 
		Recebe o bucket que sofreu overflow, sua posição no diretório e a célula que causou o overflow. */
	
	private void splitBuckets(Bucket thisBucket, int targetDir, Celula celulaExtra) { //
		CRUDIndice crudInd = new CRUDIndice();	// Cria CRUD de Índice
		int newDir = crudInd.getQtd(ind);	// Obtém a posição do novo bucket
		Celula[] thisCelulas = new Celula[thisBucket.getCelulas().length+1]; //Cria vetor de células para realizar o split
		thisCelulas = appendCelula(thisBucket.getCelulas(),celulaExtra); // Adiciona a célula extra ao vetor
		//System.out.println(Arrays.toString(thisCelulas));
		thisBucket.clear(); // Limpa o bucket atual
		thisBucket.setProfLocal(thisBucket.getProfLocal()+1); // Aumenta a profundidade local do bucket em 1
		Bucket newBucket = new Bucket(thisBucket.getProfLocal()); // Cria novo bucket com a mesma profundidade do original
		//System.out.println("**- "+ thisBucket.toString());// print
		//System.out.println("* - targetDir=" + targetDir + " newDir="+newDir);// print
		int i = 0;
		boolean falhaAoAdicionar = false;
		for(i=0;i<thisCelulas.length;i++) {
			int thisDir = dirGetIndice(thisCelulas[i].getCpf());
			//System.out.println("!*- THISDIR: "+thisDir);
			if(thisBucket.getQtd()<=thisBucket.getBUCKET_QTD() && newBucket.getQtd()<=newBucket.getBUCKET_QTD() && falhaAoAdicionar == false) {
				if(thisDir == targetDir) { // se o índice for o mesmo do bucket original
					falhaAoAdicionar = thisBucket.addCelula(thisCelulas[i]); // re-adiciona a célula no bucket original, verificando se houve outro overflow
					//System.out.println("!!**- ADDED CELL"+ thisCelulas[i].toString() +"to "+ thisBucket.toString());//
				} else if(thisDir == newDir) { // se o índice for o mesmo do bucket novo
					falhaAoAdicionar = newBucket.addCelula(thisCelulas[i]); // adiciona a célula no novo bucket, verificando se houve outro overflow
					//System.out.println("!!**- ADDED CELL"+ thisCelulas[i].toString() +"to "+ newBucket.toString());//
				}/* else { // análise
					System.out.println("ERRO AO ADICIONAR CELULA " + thisCelulas[i].toString());
				}*/
			}
		}
		crudInd.update(thisBucket, ind, targetDir); // Atualiza o bucket original na sua posição
		//System.out.println(newBucket.toString());
		crudInd.create(newBucket, ind, newDir); // Cria o bucket novo na sua posição
		diretorio.write(dir); // Atualiza o diretório no arquivo
		if(falhaAoAdicionar == true) { // Se houve um novo Overflow durante um split...
			CRUDCliente crudCli = new CRUDCliente();
			Cliente erro = crudCli.read(main, thisCelulas[4].getPos()); // Resgata o cliente que não entrou no bucket para ser re-inserido
			//System.out.println(erro.toString());
			create(erro); // Envia o cliente novamente à função de criação, garantindo que outro Split seja feito até que ele consiga entrar no Bucket.
		}
	}
	public void create(Cliente cli) {
		CRUDCliente crudCli = new CRUDCliente(); 						// Cria CRUD de Clientes
		int pos = crudCli.getNextPos(main);								// Obtém a próxima posição em que o cliente deve ser inserido	
		int cellPos = pos;
		if(pos>1 && crudCli.read(main, pos-1).getCpf() == cli.getCpf()) { // Se for uma recursão, esse cliente já foi inserido. Garante que a célula continue na posição do registro que já existe.
			cellPos = pos-1;
		}
		Celula celula = new Celula(cli.getCpf(),cellPos); 				// Cria célula para o índice
		int indPos = dirGetIndice(celula.getCpf()); 					// Obtém o índice para o bucket em que o cliente será inserido
		//System.out.println(indPos);									// print indpos 
		Bucket thisBucket = new Bucket(); 								// Cria novo bucket
		CRUDIndice crudInd = new CRUDIndice(); 							// Cria CRUD de Índice
		thisBucket = crudInd.read(ind, indPos);							// Obtém o bucket na posição destinada
		boolean cpfExists = false;
		for(int i=0;i<thisBucket.getBUCKET_QTD();i++) {					// Verifica se CPF já existe no bucket (logo, no registro)
			if(cpfExists || cli.getCpf() == thisBucket.getCelulas()[i].getCpf()) {
				cpfExists = true;
			}
		}
		if(cpfExists == false) {										// Caso CPF exista...
			if(thisBucket.getQtd()<thisBucket.getBUCKET_QTD()) {			// Caso caiba mais uma célula...
				thisBucket.addCelula(celula); 								// Adiciona célula ao Bucket
				crudInd.update(thisBucket, ind, indPos); 					// Atualiza o bucket no arquivo
				crudCli.create(cli, main); 									// cria o Cliente
				//crudInd.readAll(ind);										// print all buckets
			} else { 													// Se não caber...
				if(thisBucket.getProfLocal()>=diretorio.getProfGlobal()) { 	// Se a profundidade do bucket for igual à do diretório...
					//System.out.println("BUCKET OVERFLOW (split+dir) - "+celula.toString());
					diretorio.increaseProfGlobal(indPos, crudInd.getQtd(ind)); 	// Aumenta a profundidade do diretório
					diretorio.write(dir); 										// Atualiza diretório no arquivo
					if(crudCli.read(main, pos-1).getCpf() != cli.getCpf()) {	// Se isso não for uma recursão, não há o registro no arquivo. Inserir.
						crudCli.create(cli, main);
					}
					this.splitBuckets(thisBucket, indPos, celula); 				// Divide os Buckets
					//crudInd.readAll(ind);										// print all buckets
				} else { 													// Se não...
					//System.out.println("BUCKET OVERFLOW (split) - "+celula.toString());
					diretorio.updateBuckets(indPos, crudInd.getQtd(ind));
					diretorio.write(dir);
					if(crudCli.read(main, pos-1).getCpf() != cli.getCpf()) {	// Se isso não for uma recursão, não há o registro no arquivo. Inserir.
						crudCli.create(cli, main);
					}
					this.splitBuckets(thisBucket, indPos, celula);				// Divide os buckets
					//crudInd.readAll(ind);										// print all buckets
				}
			}
		} else {														// Se CPF já existir...
			System.out.println("CPF "+cli.getCpf()+" JÁ EXISTE!\nREGISTRO NÃO INSERIDO!");
		}
	}
	// lê o cliente no arquivo mestre que possui o CPF fornecido
	public Cliente read(int cpf) {
		CRUDCliente crudCli = new CRUDCliente();
		CRUDIndice crudInd = new CRUDIndice();
		Cliente tempCli = new Cliente();
		int cliPos;
		int indPos = dirGetIndice(cpf);
		//System.out.println("INDPOS: "+indPos+" for CPF "+cpf);//
		Bucket thisBucket = new Bucket();
		thisBucket = crudInd.read(ind, indPos);
		boolean cpfExists = false;
		int cellPos=0;
		for(int i=0;!cpfExists && i<thisBucket.getBUCKET_QTD();i++) {	// enquanto não encontrar o cliente no bucket...
			//System.out.println("CPF "+cpf+" == "+thisBucket.getCelulas()[i].getCpf()+"(pos "+thisBucket.getCelulas()[i].getPos()+") ?");//
			if(cpf == thisBucket.getCelulas()[i].getCpf()) { // se encontrou o cliente
				cpfExists = true; // cpf existe
				cellPos=i; // obtém posição no bucket
			}
		}
		if(cpfExists==true) { // se cpf foi encontrado
			cliPos = thisBucket.getCelulas()[cellPos].getPos(); // obtém posição do arquivo mestre
			tempCli = crudCli.read(main, cliPos); // lê cliente na posição
		} else { // se não
			System.out.println("CPF "+cpf+" NÃO ENCONTRADO!");
			tempCli = new Cliente(); // retorna cliente vazio
		}
		return tempCli;
	}
	// lê todos os clientes
	public void readAllClientes() {
		CRUDCliente crudCli = new CRUDCliente();
		crudCli.readAll(main);
	}
	// lê todos os buckets
	public void readAllBuckets() {
		CRUDIndice crudInd = new CRUDIndice();
		crudInd.readAll(ind);
	}
	// lê o diretório
	public void readDiretorio() {
		System.out.println(diretorio.toString());
	}
	// deleta o cliente com o cpf fornecido
	public void delete(int cpf) {
		if(cpf<0) // se cpf for negativo, define como 0
			cpf = 0;
		CRUDCliente crudCli = new CRUDCliente();
		CRUDIndice crudInd = new CRUDIndice();
		int cliPos = 0;
		int indPos = dirGetIndice(cpf);
		boolean foundCpf = false;
		Bucket thisBucket = crudInd.read(ind, indPos);
		int i;
		for(i=0;!foundCpf && i<thisBucket.getBUCKET_QTD();i++) { // enquanto não encontrar o cpf
			int thisCpf = thisBucket.getCelulas()[i].getCpf();
			if(!foundCpf && thisCpf == cpf) { // se cpf for encontrado e ainda não foi
				cliPos = thisBucket.getCelulas()[i].getPos(); // obtém posição
				thisBucket.getCelulas()[i].setCpf(-1); // zera cpf
				thisBucket.getCelulas()[i].setPos(-1); // zera posição
				foundCpf = true;
			}
		}
		if(foundCpf == true) { // se encontrou o cpf, deleta
			crudCli.delete(main, cliPos);
			thisBucket.setQtd(thisBucket.getQtd()-1);
			crudInd.update(thisBucket, ind, indPos);
			System.out.println("CPF "+cpf+" DELETADO");
		} else { // se não...
			System.out.println("CPF "+cpf+" NÃO ENCONTRADO!-!");
		}
		
	}
	public void update(int cpf, Cliente cli) {
		if(cpf<0) // se cpf for negativo, define como 0
			cpf = 0;
		CRUDCliente crudCli = new CRUDCliente();
		CRUDIndice crudInd = new CRUDIndice();
		int cliPos = 0;
		int indPos = dirGetIndice(cpf);
		boolean foundCpf = false;
		Bucket thisBucket = crudInd.read(ind, indPos);
		int i;
		for(i=0;!foundCpf && i<thisBucket.getBUCKET_QTD();i++) { // enquanto não encontrar o cpf
			int thisCpf = thisBucket.getCelulas()[i].getCpf();
			if(!foundCpf && thisCpf == cpf) { // se cpf for encontrado e ainda não foi
				cliPos = thisBucket.getCelulas()[i].getPos(); // obtém posição
				thisBucket.getCelulas()[i].setCpf(cli.getCpf()); // atualiza cpf
				foundCpf = true;
			}
		}
		if(foundCpf == true) { // se encontrou o cpf, deleta
			crudCli.update(main, cliPos, cli);
			crudInd.update(thisBucket, ind, indPos);
			System.out.println("CPF "+cpf+" ATUALIZADO");
		} else { // se não...
			System.out.println("CPF "+cpf+" NÃO ENCONTRADO!-!");
		}
	}
}
class Aplicacao {
	public static int N_TEST = 1000;
	public static Cliente atualizaCliente(Cliente temp) {
		Scanner sc = new Scanner(System.in);
		System.out.println("MODIFICANDO CLIENTE: ");
		System.out.println(temp.toString());
		System.out.println("Escolha um para modificar:");
		System.out.println("1 - NOME\n2 - NASCIMENTO\n3 - SEXO\n4 - DESCRIÇÃO\n");
		int sel = sc.nextInt();
		sc.nextLine();
		if(sel<1||sel>4) {
			System.out.println("ENTRADA INVÁLIDA!");
		} else {
				if(sel==1) {
					System.out.println("DIGITE O NOVO NOME:");
					String nome = sc.nextLine();
					temp.setNome(nome);
				} else if(sel==2) {
					System.out.println("DIGITE A NOVA DATA DE NASCIMENTO:");
					System.out.println("DIA: ");
					int dia = sc.nextInt();
					System.out.println("MÊS: ");
					int mes = sc.nextInt();
					System.out.println("ANO: ");
					int ano = sc.nextInt();
					String nascimento = (dia+"/"+mes+"/"+ano);
					System.out.println(nascimento);
					temp.setNascimento(nascimento);
				} else if(sel==3) {
					char currSex = temp.getSexo();
					if(currSex == 'M') {
						temp.setSexo('F');
					} else if(currSex == 'F'){
						temp.setSexo('M');
					}
				} else if(sel==4) {
					System.out.println("DIGITE A NOVA DESCRIÇÃO:");
					String descricao = sc.nextLine();
					temp.setDescricao(descricao);
				}
		}
		return temp;
	}
	public static Cliente preencheCliente() {
		Scanner sc = new Scanner(System.in);
		Cliente temp = new Cliente();
		temp.setDeleted(false);
		System.out.println("DIGITE O CPF: ");
		temp.setCpf(sc.nextInt());
		sc.nextLine();
		System.out.println("DIGITE O NOME: ");
		temp.setNome(sc.nextLine());
		System.out.println("DIGITE A DATA DE NASCIMENTO:");
		System.out.println("DIA: ");
		int dia = sc.nextInt();
		System.out.println("MÊS: ");
		int mes = sc.nextInt();
		System.out.println("ANO: ");
		int ano = sc.nextInt();	
		String nascimento = (dia+"/"+mes+"/"+ano);
		System.out.println(nascimento);
		temp.setNascimento(nascimento);
		int sexo=-1;
		while(sexo!=0 && sexo!=1) {
			System.out.println("INFORME O SEXO:\n0 - M\n1 - F");
			sexo = sc.nextInt();
			if(sexo == 0) {
				temp.setSexo('M');
			} else if (sexo == 1) {
				temp.setSexo('F');
			}
		}
		sc.nextLine();
		System.out.println("DIGITE A DESCRIÇÃO: ");
		temp.setDescricao(sc.nextLine());
		System.out.println("NOVO CLIENTE: ");
		System.out.println(temp.toString());
		return temp;
	}
	public static void simular() throws IOException {
		int nTests;
		CRUDAplicacao CRUD = new CRUDAplicacao("teste");
		Cliente cli;
		Scanner sc = new Scanner(System.in);
		System.out.println("Digite a quantidade de iterações: ");
		nTests = sc.nextInt();
		if(nTests <= 0)
			nTests = 1;
		long timeStart,timeEnd;
		long quickTimeStart, quickTimeEnd;
		float timeFinal, quickTimeTotal = 0;
		int opt;
		boolean continuar = true;
		boolean aleatorio = false;
		Random gerador;
		while (continuar) {
			if(aleatorio) {
				gerador = new Random(1234);
				System.out.println("QTD: "+nTests+"x (ALEATÓRIO)\n1: CRIAR\n2: LER TUDO\n3: DELETAR\n4: ALTERNAR PARA SEQUENCIAIS\n0: SAIR");
				opt = sc.nextInt();
				switch(opt) {
				case 0:
					continuar=false;
				break;
				case 1:
					timeStart = System.nanoTime();
					for(int i=1;i<nTests+1;i++) {
						int n = gerador.nextInt(999999999);
						System.out.println("("+i+") CRIANDO "+n);
						cli = new Cliente(n,"José","12/12/2001",'M',"Diagnóstico Incompleto");
						quickTimeStart = System.nanoTime();
						CRUD.create(cli);
						quickTimeEnd = System.nanoTime();
						quickTimeTotal += ((float)(quickTimeEnd - quickTimeStart))/1000000;
						//System.out.println(i + " " + quickTimeTotal); // analisar o tempo total a cada inserção
					}
					timeEnd = System.nanoTime();
					timeFinal = ((float)(timeEnd - timeStart))/1000000;
					System.out.println("Tempo de "+nTests+" Inserções: "+timeFinal+"ms");
				break;
				case 2:
					timeStart = System.nanoTime();
					CRUD.readAllClientes();
					timeEnd = System.nanoTime();
					timeFinal = ((float)(timeEnd - timeStart))/1000000;
					System.out.println("Tempo de Leitura Total: "+timeFinal+"ms");
				break;
				case 3:
					timeStart = System.nanoTime();
					for(int i=1;i<nTests+1;i++) {
						int n = gerador.nextInt(999999999);
						System.out.print("("+i+") ");
						CRUD.delete(n);
					}
					timeEnd = System.nanoTime();
					timeFinal = ((float)(timeEnd - timeStart))/1000000;
					System.out.println("Tempo de "+nTests+" Remoções: "+timeFinal+"ms");
				break;
				case 4:
					aleatorio = false;
				break;
				default:
				}
			}
			else {
				System.out.println("QTD: "+nTests+"x (SEQUENCIAL)\n1: CRIAR\n2: LER TUDO\n3: DELETAR\n4: ALTERAR PARA ALEATÓRIOS\n0: SAIR");
				opt = sc.nextInt();
				switch(opt) {
				case 0:
					continuar=false;
				break;
				case 1:
					timeStart = System.nanoTime();
					for(int i=1;i<nTests+1;i++) {
						System.out.println("("+i+") CRIANDO "+i);
						cli = new Cliente(i,"José","12/12/2001",'M',"Diagnóstico Incompleto");
						quickTimeStart = System.nanoTime();
						CRUD.create(cli);
						quickTimeEnd = System.nanoTime();
						quickTimeTotal += ((float)(quickTimeEnd - quickTimeStart))/1000000;
						//System.out.println(i + " " + quickTimeTotal); // analisar o tempo total a cada inserção
					}
					timeEnd = System.nanoTime();
					timeFinal = ((float)(timeEnd - timeStart))/1000000;
					System.out.println("Tempo de "+nTests+" Inserções: "+timeFinal+"ms");
				break;
				case 2:
					timeStart = System.nanoTime();
					CRUD.readAllClientes();
					timeEnd = System.nanoTime();
					timeFinal = ((float)(timeEnd - timeStart))/1000000;
					System.out.println("Tempo de Leitura Total: "+timeFinal+"ms");
				break;
				case 3:
					timeStart = System.nanoTime();
					for(int i=1;i<nTests+1;i++) {
						System.out.print("("+i+") ");
						CRUD.delete(i);
					}
					timeEnd = System.nanoTime();
					timeFinal = ((float)(timeEnd - timeStart))/1000000;
					System.out.println("Tempo de "+nTests+" Remoções: "+timeFinal+"ms");
				break;
				case 4:
					aleatorio = true;
				break;
				default:
				}
			}
		}
	}
	public static void CRUD() throws IOException {
		CRUDAplicacao CRUD = new CRUDAplicacao("teste");
		Scanner sc = new Scanner(System.in);
		Cliente cli;
		long timeStart,timeEnd;
		float timeFinal;
		int opt;
		int cpf;
		boolean continuar = true;
		while (continuar) {
			System.out.println("1: CRIAR\n2: LER\n3: ATUALIZAR\n4: DELETAR\n5: IMPRIMIR TUDO\n6: INICIAR SIMULAÇÃO\n0: SAIR");
			opt = sc.nextInt();
			switch(opt) {
			case 0:
				continuar=false;
			break;
			case 1:
				cli = preencheCliente();
				timeStart = System.nanoTime();
				CRUD.create(cli);
				timeEnd = System.nanoTime();
				timeFinal = ((float)(timeEnd - timeStart))/1000000;
				System.out.println("Tempo de Inserção: "+timeFinal+"ms");
			break;
			case 2:
				System.out.println("INFORME O CPF: ");
				cpf = sc.nextInt();
				timeStart = System.nanoTime();
				cli = CRUD.read(cpf);
				timeEnd = System.nanoTime();
				timeFinal = ((float)(timeEnd - timeStart))/1000000;
				if(cli.getDeleted()==false) {
					System.out.println(cli.toString());
					System.out.println("Tempo de Leitura: "+timeFinal+"ms");
				}
			break;
			case 3:
				System.out.println("INFORME O CPF: ");
				cpf = sc.nextInt();
				cli = CRUD.read(cpf);
				if (cli.getDeleted()==false) {
					Cliente temp = atualizaCliente(CRUD.read(cpf));
					timeStart = System.nanoTime();
					CRUD.update(cpf,temp);
					timeEnd = System.nanoTime();
					timeFinal = ((float)(timeEnd - timeStart))/1000000;
					System.out.println("Tempo de Atualização: "+timeFinal+"ms");
				}
			break;
			case 4:
				System.out.println("INFORME O CPF: ");
				cpf = sc.nextInt();
				timeStart = System.nanoTime();
				CRUD.delete(cpf);
				timeEnd = System.nanoTime();
				timeFinal = ((float)(timeEnd - timeStart))/1000000;
				System.out.println("Tempo de Remoção: "+timeFinal+"ms");
			break;
			case 5:
				System.out.println("DIRETÓRIO: ");
				CRUD.readDiretorio();
				System.out.println("Digite um número para continuar...");
				sc.next();
				System.out.println("ÍNDICE: ");
				CRUD.readAllBuckets();
				System.out.println("Digite um número para continuar...");
				sc.next();
				System.out.println("CLIENTES: ");
				CRUD.readAllClientes();
				System.out.println("Digite um número para continuar...");
				sc.next();
			break;
			case 6:
				simular();
			break;	
			default:
			}
		}
	}
	public static void main(String[] args) throws Exception {
		System.out.println(DIR.PATH);
		File  f = new File(DIR.PATH);
		f.mkdirs();
		CRUD();
		CabecalhoCliente teste = new CabecalhoCliente(DIR.PATH+"testeMain.txt");
		System.out.println(teste.toString());
		System.out.println("QTD de Clientes: " + teste.getQtd());
	}
}