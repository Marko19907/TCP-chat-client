package no.ntnu;

import java.util.Random;

public class Jokes {
    // Jokes from:
    // * https://parade.com/968666/parade/chuck-norris-jokes/
    // * https://www.hongkiat.com/blog/programming-jokes/
    private static final String[] JOKES = new String[]{
            "In the Beginning there was nothing ... then Chuck Norris roundhouse kicked nothing and told it to get a job.",
            "When God said, 'Let there be light!' Chuck said, 'Say Please.'",
            "If Chuck Norris were to travel to an alternate dimension in which there was another Chuck Norris and they both fought, they would both win.",
            "If you ask Chuck Norris what time it is, he always says, 'Two seconds till.' After you ask, 'Two seconds to what?' he roundhouse kicks you in the face.",
            "Since 1940, the year Chuck Norris was born, roundhouse kick related deaths have increased 13,000 percent.",
            "There is no chin behind Chuck Norris' beard. There is only another fist.",
            "Chuck Norris does not use spell check. If he happens to misspell a word, Oxford will change the spelling.",
            "Programmer: A person who fixed a problem that you didn't know you had, in a way you don't understand.",
            "Algorithm: A word used by programmers when... they do not want to explain what they did.",
            "Hardware: the part of computer that you can kick.",
            "Q: What's the object-oriented way to become wealthy? A: Inheritance",
            "Q: How do you tell an introverted computer scientist from an extroverted computer scientist? A: An extroverted computer scientist looks at your shoes when he talks to you.",
            "Chuck Norris can take a screenshot of his blue screen.",
            "A programmer had a problem. He decided to use Java. He now has a ProblemFactory.",
            "If you put a million monkeys on a million keyboards, one of them will eventually write a Java program. The rest of them will write Perl programs.",
            "3 SQL statements walked into a NoSQL bar. A little while later... they walked out because they couldn't find a table.",
            "Unix is user friendly... It's just very peculiar about who it's friends are",
            "If the box says 'This software requires Windows XP or better', does that mean it'll run on Linux?",
            "I'd like to make the world a better place... but they won't give me the source code.",
            "Hide and seek champion: ;. Since 1958"
    };

    private static final Random random = new Random();

    /**
     * Return a random joke from a list
     * @return A random joke
     */
    public static String getRandomJoke() {
        int jokeIndex = random.nextInt(JOKES.length);
        return JOKES[jokeIndex];
    }
}