import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		String window = "";
        char c;
        In in = new In (fileName);
        for (int i = 1; i <= 4; i++) {
            if (!in.isEmpty()) {
                window += in.readChar();
            }

            while (!in.isEmpty()) {
                c = in.readChar();
                if (CharDataMap.containsKey(window)== true) {
                    List probs = CharDataMap.get(window);
                    probs.update(c);
                } else {
                    List probs = new List ();
                    CharDataMap.put(window,probs);
                    probs.update(c);
                }
                window = window.substring(1);
                window +=c;
            }
            for (List probs : CharDataMap.values()) {
                calculateProbabilities(probs);
            }
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
        int totalCount = 0;
        for (int i = 0; i <probs.getSize(); i++) {
            CharData chr = probs.get(i);
            if (chr != null) {
                totalCount += chr.count;
            }
        }

        double totalCp = 0;
        for (int i = 0; i < probs.getSize(); i++) {
            CharData chr = probs.get(i);
            if (chr != null) {
                chr.p = (double)(chr.count/totalCount);
                chr.cp = (double)(totalCp + chr.p);
                totalCp = chr.cp;
            }
        }  
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        double randomValue = randomGenerator.nextDouble();
        ListIterator iterator = probs.listIterator(0);
        while (iterator.hasNext()) {
            CharData curreCharData = iterator.next();
            if (randomValue < curreCharData.cp) {
                return curreCharData.chr;
            }
        }
        return probs.get(probs.getSize()-1).chr;
    }
	

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
        StringBuilder generated = new StringBuilder(initialText);
        while (generated.length() < textLength) {
            String currentWindow = generated.substring(generated.length() - windowLength);
            List probs = CharDataMap.get(currentWindow);
            if (probs == null) break; 
            char nextChar = getRandomChar(probs);
            generated.append(nextChar);
        }
        return generated.toString();
    }
	

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		// Your code goes here
    }
}
