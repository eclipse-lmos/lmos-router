<!--
SPDX-FileCopyrightText: 2023 www.contributor-covenant.org

SPDX-License-Identifier: CC-BY-4.0
-->
# Model Benchmarking Framework

This project provides a framework for benchmarking various models, including but not limited to Language Models (LLMs). The benchmarking process involves preparing datasets for classification measurement and generating classification metrics.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Step 1: Preparing Dataset for Classification Measurement](#step-1-preparing-dataset-for-classification-measurement)
- [Step 2: Generating Classification Metrics](#step-2-generating-classification-metrics)
- [Usage](#usage)

## Prerequisites

Before you begin, ensure you have met the following requirements:

- You have installed [Kotlin](https://kotlinlang.org/docs/tutorials/command-line.html) and [Jupyter Notebook](https://jupyter.org/install).
- You have an OpenAI API key (if benchmarking LLM AgentRoutingSpec Resolver). Set the `OPENAI_API_KEY` environment variable with your OpenAI API key.
- You have Ollama installed(if benchmarking Vector AgentRoutingSpec Resolver) on your local machine. The default model is "all-minilm".
- 
## Setup

1. Clone the repository:

    ```sh
    git clone https://github.com/eclipse-lmos/lmos-router.git
    cd lmos-router
    ```

2. Set the `OPENAI_API_KEY` environment variable (if applicable):

    ```sh
    export OPENAI_API_KEY=your_openai_api_key
    ```

## Step 1: Preparing Dataset for Classification Measurement

The first step involves preparing the dataset by running the `LLMResolverBenchmark.kt` or `VectorResolverBenchmark.kt` script. This script reads an input CSV file, processes each record to generate predictions using the specified model, and writes the results to an output CSV file.

## Step 2: Generating Classification Metrics

The second step involves using the prediction file generated in Step 1 to compute classification metrics. This is done using a Jupyter Notebook.

### Notebook: `benchmarks/benchmark.ipynb`

1. Open the Jupyter Notebook:

    ```sh
    jupyter notebook benchmarks/benchmark.ipynb
    ```

2. Follow the instructions in the notebook to load the prediction file and generate classification metrics.

## Usage

1. Run the respective resolver script to generate the prediction file:

    - For LLM AgentRoutingSpec Resolver:
    ```sh
    kotlinc src/main/kotlin/llm/LLMResolverBenchmark.kt -include-runtime -d LLMResolverBenchmark.jar
    java -jar LLMResolverBenchmark.jar
    ```
    - For Vector AgentRoutingSpec Resolver:
    ```sh
    kotlinc src/main/kotlin/vector/VectorResolverBenchmark.kt -include-runtime -d VectorResolverBenchmark.jar
    java -jar VectorResolverBenchmark.jar
    ```

2. Open the Jupyter Notebook to generate classification metrics:

    ```sh
    jupyter notebook benchmarks/benchmark.ipynb
    ```