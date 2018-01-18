# Similarity & Nearest Neighbor Search algorithms in Java

[![Build Status](https://travis-ci.org/edduarte/similarity-search-java.svg?branch=master)](https://travis-ci.org/edduarte/similarity-search-java)

This library contains easy-to-use and high-performant nearest-neighbor-search
algorithms (as specified in "Mining of Massive Datasets", Cambridge University
Press, Rajaraman, A., & Ullman, J. D.) implemented in Java, which can be used to
determine the similarity between text Strings or sets of numbers (any Collection
that contains inheritors of java.lang.Number).

To achieve a higher performance, all these algorithms are implemented so that
multiple operations (counters, hashing, etc...) are executed in parallel (using
threads spawned by an ExecutorService).


## Quick Start

### Maven
```
<dependency>
    <groupId>com.edduarte</groupId>
    <artifactId>similarity-search-java</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Gradle
```
dependencies {
    compile 'com.edduarte:similarity-search-java:0.0.1'
}
```

### Usage

If you just need an easy way to figure out how similar two strings or
number-sets are, use this:

```java
// for strings
double similarity = Similarity.jaccard().of(string1, string2);

// for number sets
double similarity = Similarity.jaccard().of(set1, set2);
```

This will return a similarity coefficient, a value between 0 and 1
where 1 means the two strings or sets are exactly equal and
where 0 means they are disjoint.

In the case of string similarity, you might need to set an appropriate shingle
length based on the size of your strings. A shingle length of 2 is used by
default, which works well for short strings. The shingle length should be 5 if
your strings have the size of an email (e.g. 430 characters) or 8 if they have
the size of an average Wikipedia article (e.g. 7800 characters):

```java
// only needed for strings
double similarity = Similarity.jaccard()
        .withShingleLength(5)
        .of(string1, string2);
```

Jaccard is an exact approach, so this will always return the most accurate,
gold-standard result but at a slower speed than other approaches. Jaccard
similarity should be enough for most use-cases, but if you're dealing with a
massive number of operations in parallel (big data, data-streams), you should
go for a probabilistic approach like Minhashing or LSH, detailed in the
"Advanced" section below.


## Advanced

Every Similarity operation is available through the simple fluent / builder
interface in the Similarity class.

### Jaccard

```java
// optional parameters
double similarity = Similarity.minhash()

        // Length of n-gram shingles that are used for
        // comparison (used for strings only).
        .withShingleLength(5)

        // An executor where the kshingling and signature 
        // processing tasks are spawned. If nothing is
        // provided then it launches a new executor with
        // the cached thread pool.
        .withExecutor(executorService)

        .of(string1, string2);
```


### Minhashing

```java
// for strings
double similarity = Similarity.minhash().of(string1, string2);

// for number sets
double similarity = Similarity.minhash().of(set1, set2);

// optional parameters
double similarity = Similarity.minhash()

        // Length of n-gram shingles that are used when
        // generating signatures (used for strings only).
        .withShingleLength(5)

        // The size of the generated signatures, which are
        // compared to determine similarity.
        .withSignatureSize(100)

        // The hashing algorithm used to hash shingles to
        // signatures (used for strings only).
        .withHashMethod(HashMethod.Murmur3)

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

### LSH

Minhashing is the fastest of the implemented approaches, but returns a
non-deterministic index that can be worsened when using inappropriate shingle
lengths or signature sizes. If you need the performance of a probabilistic
approach but the deterministic index values of Jaccard similarity, you can use
Minhashing with Locality-Sensitive Hashing:

```java
// for strings
double similarity = Similarity.lsh().of(string1, string2);

// for number sets
double similarity = Similarity.lsh().of(set1, set2);

// optional parameters
double similarity = Similarity.lsh()

        // Length of n-gram shingles that are used when
        // generating signatures (used for strings only).
        .withShingleLength(5)

        // The number of bands and rows where the minhash
        // signatures will be organized.
        .withNumberOfBands(20).withNumberOfRows(5)

        // A threshold S that balances the number of false
        // positives and false negatives.
        .withThreshold(0.5)

        // The hashing algorithm used to hash shingles to
        // signatures (used for strings only).
        .withHashMethod(HashMethod.Murmur3)

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

This will return the Jaccard similarity coefficient for strings / sets that are
considered to be candidate pairs, or return 0 if they are not candidate pairs. For
a large dataset, this will only estimate the Jaccard coefficient for a smaller
subset of that data and ignore elements that are too dissimilar. This also means
that the result for candidate pairs will be deterministic while the result for
non-candidate pairs will be non-deterministic.


### Internal classes

So far the code samples have shown how to use the builder paradigm available in
the Similarity interface. However, you can instantiate a number of classes that
correspond to each step of the implemented similarity search algorithms, and use
them in your application at your own accord. Each of the converter classes below
returns a result that could, for example, be stored in a database / cache for
later use.

```java

int n = 5;
int shingleLength = 2;
int signatureSize = 100;
int bands = 20;
int rows = 5;
ExecutorService exec = Executors.newCachedThreadPool();

// generate shingles
KShingler kShingler = new KShingler(shingleLength);
List<CharSequence> shingles1 = exec.submit(kShingler.apply("example string 1")).get();
List<CharSequence> shingles2 = exec.submit(kShingler.apply("example string 2")).get();

// get jaccard similarity coefficient
double stringSimilarity = Similarity.jaccardIndex(shingles1, shingles2);
double setSimilarity    = Similarity.jaccardIndex(exampleSet1, exampleSet2);

// get signatures from shingles
KShingles2SignatureConverter c1 = new KShingles2SignatureConverter(HashMethod.Murmur3, signatureSize);
int[] stringSignature1 = exec.submit(c1.apply(shingles1)).get();
int[] stringSignature2 = exec.submit(c1.apply(shingles2)).get();

// generate a universal-hash signature for sets
Set2SignatureConverter c2 = new Set2SignatureConverter(n, signatureSize);
int[] setSignature1 = exec.submit(c2.apply(exampleSet1)).get();
int[] setSignature2 = exec.submit(c2.apply(exampleSet2)).get();

// get minhash similarity coefficient
double stringSimilarity = Similarity.signatureIndex(stringSignature1, stringSignature2);
double setSimilarity    = Similarity.signatureIndex(setSignature1, setSignature2);

// convert signatures to bands
Signature2BandsConverter c3 = new Signature2BandsConverter(bands, rows);
int[] stringBands1 = exec.submit(c3.apply(stringSignature1)).get();
int[] stringBands2 = exec.submit(c3.apply(stringSignature2)).get();
int[] setBands1 = exec.submit(c3.apply(setSignature1)).get();
int[] setBands2 = exec.submit(c3.apply(setSignature2)).get();

// determine if there are any candidate pairs
boolean isCandidatePair = Similarity.isCandidatePair(stringBands1, stringBands2);
boolean isCandidatePair = Similarity.isCandidatePair(setBands1, setBands2);

```

Note that all of the converter classes above return a Callable, which can be
submitted into a single Executor in order to trigger multiple conversion calls
in parallel.


## Projects using this library

You can see this library in use at https://github.com/vokter/vokter.


# License

    Copyright 2017 Eduardo Duarte

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

