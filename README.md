# Smart Home Voice Navigator

An Android-based Smart Home application that utilizes Edge-AI to process and classify voice commands entirely offline. 

This project implements a complete Machine Learning pipeline—from training a custom Convolutional Neural Network (CNN) in PyTorch to deploying it on a mobile device using ONNX Runtime. A key technical achievement of this project is the native Kotlin implementation of Fast Fourier Transforms (FFT) and Mel Spectrogram generation, bypassing the need for unstable external audio processing libraries on Android.

## Key Features

* **100% Offline Edge Inference:** No cloud APIs or internet connection required. All processing and predictions happen locally on the device, ensuring user privacy and zero network latency.
* **Native Signal Processing:** Audio captured from the microphone is transformed into Mel Spectrograms in real-time using a custom, highly optimized math pipeline written purely in Kotlin.
* **Custom MobileNetV2 Architecture:** The acoustic model is based on a fine-tuned MobileNetV2, selected for its depthwise separable convolutions which provide an optimal balance between high accuracy (94.2% F1-Score) and a minimal parameter footprint.
* **Smart Noise Filtering & Auto-Focus:** Implements an energy-based audio scanning algorithm to accurately isolate the user's voice from background noise.
* **Confidence Thresholding:** Mimics real-world virtual assistants by applying a Softmax function to the model's logits, discarding predictions with low confidence to prevent false positives.
* **Dynamic UI Navigation:** The Smart Home dashboard visually reacts in real-time to the recognized commands (e.g., *on, off, up, down, stop*).

## Tech Stack

* **Mobile App:** Kotlin, Android SDK, XML
* **Machine Learning:** PyTorch, Torchaudio, Weights & Biases (W&B)
* **Model Deployment:** ONNX, ONNX Runtime Mobile
* **Audio Processing:** Native Kotlin Math (Hann Window, FFT, Mel Filter Banks)

## Architecture & Pipeline

1. **Audio Capture:** The app records 16kHz PCM audio blocks.
2. **Pre-processing:** The raw waveform is converted into a Mel Spectrogram (64 mels x 32 frames) natively in Kotlin.
3. **Inference:** The flattened spectrogram is fed into the ONNX Runtime session containing the trained MobileNetV2 weights.
4. **Post-processing:** The output logits are passed through a Softmax function to determine the probability. If the confidence exceeds 75%, the command is executed.

## Installation & Usage

1. Clone the repository.
2. Open the project in **Android Studio**.
3. Ensure the ONNX model (`.onnx`) and its weights (`.data`) are located in the `app/src/main/assets/` directory.
4. Sync the Gradle files to download the ONNX Runtime dependencies.
5. Build and run the application on a physical Android device (API 24+ recommended for microphone testing).

## Author

**Steven Ignacio Sequeira Araya** | Computer Engineering
