import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
  public static void main(String[] args) {
    int maxSize = 100;
    int textNumber = 10_000;
    int textLength = 100_000;
    ArrayList<String> lettersArray = new ArrayList<>(Arrays.asList("a", "b", "c"));
    ConcurrentHashMap<String, BlockingQueue<String>> lettersList = new ConcurrentHashMap<>();
    lettersList.put("a", new LinkedBlockingQueue<>(maxSize));
    lettersList.put("b", new LinkedBlockingQueue<>(maxSize));
    lettersList.put("c", new LinkedBlockingQueue<>(maxSize));
    ConcurrentHashMap<Character, String> scoreList = new ConcurrentHashMap<>();
    scoreList.put('a', "");
    scoreList.put('b', "");
    scoreList.put('c', "");
    List<Thread> processingThreads = new ArrayList<>();

    Runnable generatingString = () -> {
      for (int i = 0; i < textNumber; i++) {
        try {
          for (String letter : lettersArray) {
            lettersList.get(letter).put(generateText("abc", textLength));
          }
        } catch (InterruptedException e) {
          return;
        }
      }
    };

    Runnable searchLongestA = () -> {
      scoreList.put('a', searchLongestWord(lettersList.get("a"), 'a'));
    };

    Runnable searchLongestB = () -> {
      scoreList.put('b', searchLongestWord(lettersList.get("b"), 'b'));
    };

    Runnable searchLongestC = () -> {
      scoreList.put('c', searchLongestWord(lettersList.get("c"), 'c'));
    };

    processingThreads.add(new Thread(generatingString));
    processingThreads.add(new Thread(searchLongestA));
    processingThreads.add(new Thread(searchLongestB));
    processingThreads.add(new Thread(searchLongestC));

    startThreads(processingThreads, scoreList);
  }

  public static void startThreads(List<Thread> processingThreads, ConcurrentHashMap<Character, String> scoreList) {
    for (Thread thread : processingThreads) {
      thread.start();
    }

    for (Thread thread : processingThreads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    renderAnswers(scoreList);
  }

  public static String searchLongestWord(BlockingQueue<String> names, char searchChar) {
    String name;
    int localBigestNumber = 0;
    String localBigestWord = "";
    try {
      name = names.take();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    if(localBigestNumber < countLetter(name, searchChar)) {
      localBigestNumber = countLetter(name, searchChar);
      localBigestWord = name;
    }

    return "Количество букв " + searchChar + " = " + localBigestNumber + " " + localBigestWord;
  }

  public static void renderAnswers(ConcurrentHashMap<Character, String> scoreList) {
    System.out.println("Слово, в котором содержится максимальное количество букв А, выглядит так \n" + scoreList.get('a'));
    System.out.println("Слово, в котором содержится максимальное количество букв B, выглядит так \n" + scoreList.get('b'));
    System.out.println("Слово, в котором содержится максимальное количество букв C, выглядит так \n" + scoreList.get('c'));
  }

  public static String generateText(String letters, int length) {
    Random random = new Random();
    StringBuilder text = new StringBuilder();
    for (int i = 0; i < length; i++) {
      text.append(letters.charAt(random.nextInt(letters.length())));
    }
    return text.toString();
  }

  public static int countLetter(String line, char targetLetter) {
    return (int) line.chars()
      .filter(c -> c == targetLetter)
      .count();
  }
}
