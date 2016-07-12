# Near Neighbor Search

Near Neighbor Search algorithms (as specified in "Mining of Massive Datasets", Cambridge University Press, Rajaraman, A., & Ullman, J. D.) implemented in Java. A similarity coefficient can be determined between two strings or two sets of numbers (any Collection that contains inheritors of java.lang.Number). This coefficient is a value between 0 and 1, where 0 means the two strings / sets are disjoint and 1 means they are equal.

## Usage

### Maven
```
<dependency>
    <groupId>com.edduarte</groupId>
    <artifactId>near-neighbor-search</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```
dependencies {
    compile 'com.edduarte:near-neighbor-search:1.0.0'
}
```

## Example

### Simple

If you just need an easy way to get a similarity coefficient, use this:

```java
// for strings
double similarity = Similarity.jaccard().of(string1, string2);

// for number sets
double similarity = Similarity.jaccard().of(set1, set2);
```

You might need to set an appropriate shingle length based on the size of your strings. A shingle length of 2 is used by default, but ideally the shingle length should be 5 if your strings have the size of an email (e.g. 430 characters) or 8 if they have the size of an average Wikipedia article (e.g. 7800 characters):

```java
// only needed for strings
double similarity = Similarity.jaccard()
        .withShingleLength(5)
        .of(string1, string2);
```

### Advanced

#### Jaccard

Aside from the shingle length, you can use other optional parameters:

```java
// optional parameters
double similarity = Similarity.minhash()

        // Length of n-gram shingles that are used for
        // comparison (used for strings only).
        .withShingleLength(5)

        // An executor where the kshingling and signature 
        // processing tasks are spawned. If nothing is
        // provided then it launches a new executor with the
        // cached thread pool.
        .withExecutor(executorService)

        .of(string1, string2);
```


#### Minhashing

Jaccard is an exact approach, so this will always return the most accurate, gold-standard result but at a slower speed than other approaches. Jaccard similarity should be enough for most use-cases, but if you're dealing with a massive number of operations in parallel (big data, data-streams), you should go for a probabilistic approach like Minhashing:

```java
// for strings
double similarity = Similarity.minhash().of(string1, string2);

// for number sets
double similarity = Similarity.minhash().of(set1, set2);

// optional parameters
double similarity = Similarity.minhash()

        // The size of the generated signatures, which are
        // compared to determine similarity.
        .withSignatureSize(100)

        // Length of n-gram shingles that are used when
        // generating signatures (used for strings only).
        .withShingleLength(5)

        // The hashing algorithm used to hash shingles to
        // signatures (used for strings only). Uses murmur3
        // by default.
        .withHashMethod(14)

        // Number of unique elements in both sets (used for
        // sets only). For example, if set1=[4, 5, 6, 7, 8]
        // and set2=[7, 8, 9, 10], this value should be 7. If
        // nothing is provided, this value is determined in 
        // pre-processing.
        .withNumberOfElements(14)

        // An executor where the kshingling and signature 
        // processing tasks are spawned. If nothing is
        // provided then it launches a new executor with the
        // cached thread pool.
        .withExecutor(executorService)

        .of(string1, string2);
```

#### LSH

Minhashing is the fastest of the implemented approaches, but returns a non-deterministic index that can be worsened when using inappropriate shingle lengths or signature sizes. If you need the speed of a probabilistic approach but the deterministic index values of Jaccard similarity, you can use Minhashing with Locality-Sensitive Hashing:

```java
// for strings
double similarity = Similarity.lsh().of(string1, string2);

// for number sets
double similarity = Similarity.lsh().of(set1, set2);

// optional parameters
double similarity = Similarity.lsh()

        // The number of bands and rows where the minhash
        // signatures will be organized.
        .withNumberOfBands(20).withNumberOfRows(5)

        // A threshold S that balances the number of false
        // positives and false negatives.
        .withThreshold(0.5)

        // Length of n-gram shingles that are used when
        // generating signatures (used for strings only).
        .withShingleLength(5)

        // The hashing algorithm used to hash shingles to
        // signatures (used for strings only). Uses murmur3
        // by default.
        .withNumberOfElements(14)

        // Number of unique elements in both sets (used for
        // sets only). For example, if set1=[4, 5, 6, 7, 8]
        // and set2=[7, 8, 9, 10], this value should be 7. If
        // nothing is provided, this value is determined in 
        // pre-processing.
        .withHashMethod(14)

        // An executor where the kshingling and signature 
        // processing tasks are spawned. If nothing is
        // provided then it launches a new executor with the
        // cached thread pool.
        .withExecutor(executorService)

        .of(string1, string2);
```

This will return the Jaccard similarity coefficient for strings / sets that are considered as candidate pairs, or return 0 if they are not candidate pairs. For a large dataset, this will estimate the Jaccard coefficient for a potentially smaller subset and ignore elements that are too dissimilar. This means that the result for candidate pairs will be deterministic while the result for non-candidate pairs will be non-deterministic.

#### Internal classes

So far the code samples have shown how to use the fluent interface available in the Similarity class. However, you can instantiate a number of classes that correspond to each step of the implemented near-neighbor search algorithms, and use them in your platform at your own accord.

```java

int n = 5;
int shingleLength = 2;
int signatureSize = 100;
int bands = 20;
int rows = 5;
ExecutorService exec = Executors.newCachedThreadPool();

// generate shingles so they can be stored
KShingler kShingler = new KShingler(shingleLength);
List<CharSequence> shingles = exec.submit(kShingler.apply(string)).get();

// store the shingles
// ...

// at a later date, get jaccard similarity
double similarity = JaccardStringSimilarity.shingleSimilarity(shingles, otherShingles);

// get signatures from shingles
KShingles2SignatureConverter c = new KShingles2SignatureConverter(HashMethod.Murmur3, signatureSize);
int[] stringSignature = exec.submit(c.apply(shingles)).get();

// generate a universal-hash signature for sets
Set2SignatureConverter c = new Set2SignatureConverter(n, signatureSize);
int[] setSignature = exec.submit(c.apply(set)).get();

// store the signatures
// ...

// at a later date, get minhash similarity
double similarity = MinHashSimilarity.signatureSimilarity(stringSignature, otherStringSignature);

// convert signatures to bands
Signature2BandsConverter c = new Signature2BandsConverter(bands, rows);
int[] stringBands = exec.submit(c.apply(stringSignature)).get();
int[] setBands = exec.submit(c.apply(setSignature)).get();

// store the bands
// ...

// at a later date, determine if there are any candidate pairs
boolean isCandidatePair = LSHSimilarity.isCandidatePair(stringBands, otherStringBands);

```

Note that all of the converter classes above return a Callable, which can be submitted into a single Executor in order to trigger multiple conversion calls in parallel.


## Projects using this library

You can see this library in use at https://github.com/vokter/vokter.


# License

    Copyright 2016 Eduardo Duarte

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

