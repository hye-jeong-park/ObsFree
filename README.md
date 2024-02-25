# **🤝 Introducing Our Solution**

---

<img width="365" alt="obsfree_logo" src="https://github.com/aDecay/ObsFree/assets/136609617/3c8f2241-e80d-487c-801d-fcd6c54a56aa">

ObsFree provides a service that assists blind individuals in reporting issues with Braille blocks. By simplifying the reporting procedure, it helps the handling agency to operate more smoothly. In addition, it provides a service that shows the status of complaint acceptance and sends notifications if there may be damaged Braille blocks nearby when approaching the area. Even if a complaint is filed, it takes some time to repair it due to physical limitations. Therefore, it can reduce accidents caused by damaged or vandalized Braille blocks by notifying them for a certain period of time.

<img width="799" alt="statistics" src="https://github.com/aDecay/ObsFree/assets/136609617/91922d52-bf35-4066-aa9b-1dd7c19f576d">

# **🎯 Target UN_SGDs**

---

<img width="792" alt="un_targets" src="https://github.com/aDecay/ObsFree/assets/136609617/b999cad6-e984-4638-9d68-182ddb6fe653">

---

# **⚙**Overall Architecture

---

<img width="773" alt="overall_architecture" src="https://github.com/aDecay/ObsFree/assets/136609617/732bfbbe-07ec-4ab5-b2d5-2c4e0fc4f744">

# **📌** Implementation

## Mobile (Android)

---

### 1. Tech Stack

- AndroidX Core 1.9.0
- AndroidX AppCompat 1.6.1
- CameraX 1.3.1
- GMS play-services-location 21.1.0
- Firebase Android BoM 32.7.1
- Tensorflow Lite 2.14.0

### 2. Features

- Report damaged braille blocks
    - Camera guiding with tensorflow lite in TTS
    - Upload photo and location to firebase
- Show damaged braille blocks
    - Show location in markers
    - Change fix status
- Alert for nearby damaged braille blocks
    - Background alert using WorkManager
    - Alert in TTS

## AI

---

### 1. Tech Stack

- Colab T4 GPU
- Tensorflow lite
- Roboflow

### 2. Model: YOLOv5

- optimizer: SGD
- hyperparameters: lr0=0.01, lrf=0.01, momentum=0.937, weight_decay=0.0005, warmup_epochs=3.0, warmup_momentum=0.8, warmup_bias_lr=0.1, box=0.05, cls=0.5, cls_pw=1.0, obj=1.0, obj_pw=1.0, iou_t=0.2, anchor_t=4.0, fl_gamma=0.0, hsv_h=0.015, hsv_s=0.7, hsv_v=0.4, degrees=0.0, translate=0.1, scale=0.5, shear=0.0, perspective=0.0, flipud=0.0, fliplr=0.5, mosaic=1.0, mixup=0.0, copy_paste=0.0
- Input image size: 640x640
- Output
    - image size: 600x399
    - classes: 1 (White Cane)

### 3. Dataset

- A person with white cane
    - total: 600
    - train: 500
    - valid: 95
    - test: 5

# **📱** How to use

---

<img src="https://github.com/aDecay/ObsFree/assets/136609617/ca32544f-45b0-4d79-acac-79d43b1e68f9" width="800" height="450"/>   


<img src="https://github.com/aDecay/ObsFree/assets/136609617/424a3a20-e816-4c9c-9ea9-8712988a0342" width="800" height="450"/>   


<img src="https://github.com/aDecay/ObsFree/assets/136609617/dbd1583a-7123-49b4-aa1b-3919636b5035" width="700" height="350"/>   



# **🎥** Demo Video

---

[![Watch the video](https://img.youtube.com/vi/Y5jXek-8qhs/maxresdefault.jpg)](https://youtu.be/Y5jXek-8qhs?si=dLfZ1J7es8HvlDu4)

# **🧑‍💻 Member**

---

| Name | HyeJoengPark | DoHyunLim | YoungHyunPark | KiJinKim |
| --- | --- | --- | --- | --- |
| Role | Backend & Design | AI | Backend | Frontend |
| Profile Image | <img width="461" alt="박혜정" src="https://github.com/aDecay/ObsFree/assets/136609617/53096900-73ae-44fe-b28a-69d6da603b5a"> | ![임도현](https://github.com/aDecay/ObsFree/assets/136609617/8f6905d9-c66f-4770-a612-adc30d8590c8) | ![박영현](https://github.com/aDecay/ObsFree/assets/136609617/d5d5d584-1095-40b4-b506-acb92e4b50de) | ![김기진](https://github.com/aDecay/ObsFree/assets/136609617/19db49a1-3bc5-46c7-87e8-1c6efd77b946)
