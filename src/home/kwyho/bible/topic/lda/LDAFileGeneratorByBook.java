package home.kwyho.bible.topic.lda;

import home.kwyho.bible.data.AbbreviationHashTable;
import home.kwyho.bible.data.AbstractBibleDAO;
import home.kwyho.bible.data.BibleBook;
import home.kwyho.bible.data.BibleChapter;
import home.kwyho.bible.data.BibleVerse;
import home.kwyho.bible.data.KJV.KJVBibleDAO;
import home.kwyho.stopwords.StopWordCollections;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;

public class LDAFileGeneratorByBook {
	private AbstractBibleDAO bibleDAO;
	private StopWordCollections stopWordCollector;
	
	public LDAFileGeneratorByBook(AbstractBibleDAO bibleDAO,
			StopWordCollections stopWordCollector) {
		super();
		this.bibleDAO = bibleDAO;
		this.stopWordCollector = stopWordCollector;
	}

	public AbstractBibleDAO getBibleDAO() {
		return bibleDAO;
	}

	public void setBibleDAO(AbstractBibleDAO bibleDAO) {
		this.bibleDAO = bibleDAO;
	}

	public StopWordCollections getStopWordCollector() {
		return stopWordCollector;
	}

	public void setStopWordCollector(StopWordCollections stopWordCollector) {
		this.stopWordCollector = stopWordCollector;
	}
	
	public String processSentence(String sentence) {
		List<String> tokens = Arrays.asList(sentence.replaceAll("[^a-zA-Z]", " ").trim().toLowerCase().split(" "));
		StringBuffer stemmedSentence = new StringBuffer(" ");
		for (String token: tokens) {
			if (!stopWordCollector.isStopWord(token)) {
				stemmedSentence.append(PorterStemmerTokenizerFactory.stem(token)+" ");
			}
		}
		return stemmedSentence.toString().replaceAll("\\s+", " ");
	}
	
	public String getBookText(String bookAbbr) {
		if (!AbbreviationHashTable.getHashTable().keySet().contains(bookAbbr)) {
			return "";
		}
		BibleBook book = bibleDAO.getBook(bookAbbr);
		StringBuffer text = new StringBuffer("");
		
		for (BibleChapter chapter: book.getChapters()) {
			for (BibleVerse verse: chapter.getVerses()) {
				text.append(verse.getPassage()+" ");
			}
		}
		return text.toString();
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		KJVBibleDAO kjvBibleDAO = new KJVBibleDAO();
		kjvBibleDAO.loadSerializedBible();
		StopWordCollections stopWordsCollector = new StopWordCollections("/Users/hok1/Documents/NLP_Data/stop-words-english4.txt");
		LDAFileGeneratorByBook ldaGenerator = new LDAFileGeneratorByBook(kjvBibleDAO, stopWordsCollector);
		File wholeBible = new File("wholekjvbible.dat");
		FileWriter writer = new FileWriter(wholeBible);
		writer.write("66\n");
		for (String abbr: AbbreviationHashTable.getHashTable().keySet()) {
			writer.write(ldaGenerator.processSentence(ldaGenerator.getBookText(abbr)).trim()+"\n");
		}
		writer.close();
	}

}
