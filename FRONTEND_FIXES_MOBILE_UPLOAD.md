# FRONTEND CODE ISSUES - Mobile Greeting Upload

## Critical Issues Found in Your React Form Code

### Issue 1: ❌ Missing `lib/api.ts` Implementation
Your form imports:
```typescript
import { submitVideoMessage } from "@/lib/api";
```

But this function likely doesn't exist or has issues. Based on the backend controller, it should look like this:

**Create: `src/lib/api.ts`** (or wherever your API file is)

```typescript
export interface SubmitVideoMessageParams {
  name: string;
  message: string;
  qrId: string;
}

export async function submitVideoMessage(
  params: SubmitVideoMessageParams,
  videoBlob: Blob | null
): Promise<any> {
  if (!videoBlob) {
    throw new Error("No video recorded");
  }

  const formData = new FormData();
  
  // Append form fields
  formData.append("name", params.name);
  formData.append("message", params.message);
  
  // Append video file
  formData.append("video", videoBlob, "message.mp4");

  // Make API call
  const response = await fetch(
    `https://celebrations.tanishq.co.in/greetings/${params.qrId}/upload`,
    {
      method: "POST",
      body: formData,
      // DO NOT set Content-Type header - browser will set it with boundary
    }
  );

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "Failed to submit video");
  }

  return response.text();
}
```

### Issue 2: ❌ Missing Error Display to User

Your form catches errors but doesn't show them properly:

```typescript
catch (error) {
  console.error("Submission error:", error);
  // Optionally set form-level error here
  setErrors((prev) => ({ ...prev, message: "Failed to submit. Please try again." }));
}
```

This just logs to console. On mobile, users can't see console logs!

**Fix:**
```typescript
const [submitError, setSubmitError] = useState<string>("");

const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setSubmitError(""); // Clear previous errors

  if (!validateForm()) {
    return;
  }

  try {
    await mutation.mutateAsync(formData as FormValues);
    navigate("/video-message", {
      state: { name: formData.name, message: formData.message },
    });
  } catch (error) {
    console.error("Submission error:", error);
    const errorMessage = error instanceof Error 
      ? error.message 
      : "Failed to submit. Please try again.";
    setSubmitError(errorMessage);
  }
};
```

### Issue 3: ❌ No Video Validation Before Submit

Your form doesn't check if video exists before submitting:

**Fix:**
```typescript
const validateForm = (): boolean => {
  const newErrors: FormErrors = {};

  // Video validation
  if (!recordedVideoBlob) {
    setSubmitError("Please record a video before submitting.");
    return false;
  }

  // Name validation
  if (!formData.name.trim()) {
    newErrors.name = "Name is required";
  } else if (formData.name.trim().length < 2) {
    newErrors.name = "Name must be at least 2 characters";
  }

  // Message validation
  if (!formData.message.trim()) {
    newErrors.message = "Custom message is required";
  } else if (formData.message.trim().length < 10) {
    newErrors.message = "Message must be at least 10 characters";
  }

  setErrors(newErrors);
  return Object.keys(newErrors).length === 0;
};
```

### Issue 4: ⚠️ Mobile-Specific Text Input Issues

Mobile keyboards can cause text encoding issues. Add sanitization:

```typescript
const sanitizeText = (text: string): string => {
  return text
    .trim()
    .replace(/[\u0000-\u0008\u000B-\u000C\u000E-\u001F\u007F]/g, '') // Remove control chars
    .replace(/\s+/g, ' '); // Normalize whitespace
};

const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setSubmitError("");

  if (!validateForm()) {
    return;
  }

  try {
    // Sanitize inputs before sending
    const sanitizedData = {
      name: sanitizeText(formData.name),
      message: sanitizeText(formData.message),
    };
    
    await mutation.mutateAsync(sanitizedData as FormValues);
    navigate("/video-message", {
      state: { name: sanitizedData.name, message: sanitizedData.message },
    });
  } catch (error) {
    console.error("Submission error:", error);
    const errorMessage = error instanceof Error 
      ? error.message 
      : "Failed to submit. Please try again.";
    setSubmitError(errorMessage);
  }
};
```

## Complete Fixed UserForm Component

Here's the complete fixed version with all issues resolved:

```typescript
import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useVideo } from "@/contexts/VideoContext";
import { useMutation } from "@tanstack/react-query";
import { submitVideoMessage } from "@/lib/api";
import TanishqLogo from "@/components/TanishqLogo";
import BackgroundGradients from "@/components/BackgroundGradients";

interface FormValues {
  name: string;
  message: string;
}

interface FormErrors {
  name?: string;
  message?: string;
}

// Text sanitization for mobile input
const sanitizeText = (text: string): string => {
  return text
    .trim()
    .replace(/[\u0000-\u0008\u000B-\u000C\u000E-\u001F\u007F]/g, '') // Remove control chars
    .replace(/\s+/g, ' '); // Normalize whitespace
};

export default function UserForm() {
  const navigate = useNavigate();
  const { recordedVideoUrl, recordedVideoBlob } = useVideo();
  const videoRef = useRef<HTMLVideoElement>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const { qrId } = useVideo();
  
  const [formData, setFormData] = useState<FormValues>({
    name: "",
    message: "",
  });
  
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitError, setSubmitError] = useState<string>("");
  
  // use react-query for submission
  const mutation = useMutation<any, Error, FormValues>({
    mutationFn: (data: FormValues) => submitVideoMessage({ ...data, qrId }, recordedVideoBlob),
  });

  // Redirect if no video recorded
  useEffect(() => {
    if (!recordedVideoUrl && !recordedVideoBlob) {
      navigate("/");
    }
  }, [recordedVideoUrl, recordedVideoBlob, navigate]);

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};
    setSubmitError(""); // Clear submit error

    // Video validation
    if (!recordedVideoBlob) {
      setSubmitError("Please record a video before submitting.");
      return false;
    }

    // Name validation
    if (!formData.name.trim()) {
      newErrors.name = "Name is required";
    } else if (formData.name.trim().length < 2) {
      newErrors.name = "Name must be at least 2 characters";
    }

    // Message validation
    if (!formData.message.trim()) {
      newErrors.message = "Custom message is required";
    } else if (formData.message.trim().length < 10) {
      newErrors.message = "Message must be at least 10 characters";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: keyof FormValues, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    // Clear errors when user starts typing
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
    if (submitError) {
      setSubmitError("");
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      // Sanitize inputs before sending
      const sanitizedData = {
        name: sanitizeText(formData.name),
        message: sanitizeText(formData.message),
      };
      
      console.log("Submitting:", { qrId, sanitizedData });
      
      await mutation.mutateAsync(sanitizedData as FormValues);
      
      // On success, navigate to video message
      navigate("/video-message", {
        state: { name: sanitizedData.name, message: sanitizedData.message },
      });
    } catch (error) {
      console.error("Submission error:", error);
      const errorMessage = error instanceof Error 
        ? error.message 
        : "Failed to submit. Please try again.";
      setSubmitError(errorMessage);
    }
  };

  const handleCancel = () => {
    navigate("/recording");
  };

  const handlePlayPause = () => {
    if (videoRef.current) {
      if (isPlaying) {
        videoRef.current.pause();
      } else {
        videoRef.current.play();
      }
      setIsPlaying(!isPlaying);
    }
  };

  return (
    <div className="relative min-h-screen bg-white text-tanishq-primary-500 overflow-hidden max-w-md mx-auto">
      <BackgroundGradients />

      <div className="relative z-10 flex flex-col items-center px-6 pt-4">
        <div className="mb-4">
          <TanishqLogo className="w-11 h-7 text-tanishq-primary-400" />
        </div>

        <div className="w-full max-w-[338px] space-y-4">
          <h1 className="text-lg text-center text-tanishq-primary-500 font-fraunces font-normal leading-[127%] tracking-[-0.18px] max-w-[319px] mx-auto mb-6">
            Let them know it's from you—complete the form.
          </h1>

          {/* Global submit error message */}
          {submitError && (
            <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-600 text-sm">{submitError}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Name input */}
            <div className="space-y-1">
              <div
                className={`flex items-center gap-3 px-5 py-3 border rounded-full ${
                  errors.name ? "border-red-500" : "border-tanishq-primary-400"
                }`}
              >
                <svg
                  className="w-3 h-4 text-tanishq-neutral-600 flex-shrink-0"
                  viewBox="0 0 12 16"
                  fill="none"
                >
                  <path
                    d="M11.5 15C11.5 12.9382 9.03756 11.2667 6 11.2667C2.96243 11.2667 0.5 12.9382 0.5 15M6 8.46667C3.97495 8.46667 2.33333 6.79516 2.33333 4.73333C2.33333 2.67147 3.97495 1 6 1C8.02501 1 9.66667 2.67147 9.66667 4.73333C9.66667 6.79516 8.02501 8.46667 6 8.46667Z"
                    stroke="currentColor"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
                <input
                  type="text"
                  placeholder="Type your name"
                  value={formData.name}
                  onChange={(e) => handleInputChange("name", e.target.value)}
                  className={`flex-1 bg-transparent font-ibm-plex-sans text-sm font-normal leading-[127%] tracking-[-0.14px] outline-none placeholder:text-tanishq-neutral-500 ${
                    errors.name ? "text-red-600" : "text-tanishq-neutral-500"
                  }`}
                />
              </div>
              {errors.name && (
                <p className="text-red-500 text-xs px-5">{errors.name}</p>
              )}
            </div>

            {/* Message textarea */}
            <div className="space-y-1">
              <div
                className={`flex items-start gap-3 p-5 border rounded-md ${
                  errors.message
                    ? "border-red-500"
                    : "border-tanishq-primary-500"
                }`}
              >
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M18.2469 5.0136L9.71288 13.5476C8.86305 14.3974 6.34039 14.791 5.77683 14.2274C5.21326 13.6639 5.59792 11.1412 6.44774 10.2914L14.9907 1.74846C15.2014 1.51862 15.4564 1.33386 15.7405 1.20531C16.0246 1.07676 16.3318 1.00708 16.6435 1.00051C16.9552 0.993953 17.265 1.05061 17.5542 1.16709C17.8435 1.28357 18.1061 1.45747 18.3262 1.67825C18.5464 1.89902 18.7196 2.16211 18.8352 2.45164C18.9509 2.74118 19.0068 3.05114 18.9994 3.36285C18.992 3.67456 18.9214 3.98156 18.7921 4.26527C18.6628 4.54897 18.4773 4.80355 18.2469 5.0136Z" stroke="#56544E" strokeLinecap="round" strokeLinejoin="round" />
                  <path d="M9.05096 2.8667H4.57821C3.6292 2.8667 2.71913 3.24368 2.04808 3.91473C1.37704 4.58578 1 5.4959 1 6.44491V15.3904C1 16.3395 1.37704 17.2496 2.04808 17.9206C2.71913 18.5917 3.6292 18.9686 4.57821 18.9686H14.4183C16.3952 18.9686 17.1019 17.3584 17.1019 15.3904V10.9177" stroke="#56544E" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                
                <textarea
                  placeholder="Type your custom message"
                  value={formData.message}
                  onChange={(e) => handleInputChange("message", e.target.value)}
                  rows={3}
                  className={`flex-1 bg-transparent font-ibm-plex-sans text-sm font-normal leading-[127%] tracking-[-0.14px] outline-none placeholder:text-tanishq-neutral-500 resize-none ${
                    errors.message ? "text-red-600" : "text-tanishq-neutral-500"
                  }`}
                />
              </div>
              {errors.message && (
                <p className="text-red-500 text-xs px-5">{errors.message}</p>
              )}
            </div>
          </form>
        </div>

        {/* Recorded Video Display */}
        <div className="relative w-[335px] h-[272px] mt-6 rounded-[25px] overflow-hidden bg-black">
          {recordedVideoUrl ? (
            <>
              <video
                ref={videoRef}
                src={recordedVideoUrl}
                className="w-full h-full object-cover"
                onClick={handlePlayPause}
                onPlay={() => setIsPlaying(true)}
                onPause={() => setIsPlaying(false)}
                onEnded={() => setIsPlaying(false)}
                preload="metadata"
                playsInline
                disablePictureInPicture
              />
              
              {!isPlaying && (
                <button
                  onClick={handlePlayPause}
                  className="absolute inset-0 flex items-center justify-center bg-black/20"
                >
                  <div className="w-20 h-20 rounded-full bg-tanishq-primary-600/20 flex items-center justify-center">
                    <svg
                      className="w-6 h-6 text-tanishq-primary-400 ml-1"
                      viewBox="0 0 78 77"
                      fill="none"
                    >
                      <path
                        d="M30 28.863V48.137C30 49.6067 31.5436 50.4997 32.7324 49.6997L47.175 40.0628C48.275 39.3372 48.275 37.6628 47.175 36.9186L32.7324 27.3003C31.5436 26.5003 30 27.3933 30 28.863Z"
                        fill="currentColor"
                      />
                    </svg>
                  </div>
                </button>
              )}
            </>
          ) : (
            <div className="w-full h-full flex items-center justify-center bg-tanishq-primary-25">
              <p className="text-tanishq-primary-500 text-center">
                No video recorded
              </p>
            </div>
          )}
        </div>

        {/* Action buttons */}
        <div className="flex gap-4 mt-6 w-full max-w-[326px] mb-8">
          <button
            type="button"
            onClick={handleCancel}
            className="flex items-center justify-center gap-2 px-4 py-3 border border-tanishq-primary-400 bg-tanishq-primary-25 rounded-[80px] text-tanishq-primary-900 font-fraunces text-lg sm:text-xl font-normal leading-[128%] tracking-[-0.4px] hover:bg-tanishq-primary-25/80 transition-colors flex-1"
          >
            Cancel
            <div className="w-8 h-8 rounded-full flex items-center justify-center">
              <svg className="w-4 h-4" viewBox="0 0 42 42" fill="none">
                <path
                  d="M13.8874 14.3874C13.3709 14.904 13.3709 15.7414 13.8874 16.258L18.6294 21L13.8874 25.7421C13.3709 26.2586 13.3709 27.0961 13.8874 27.6126C14.4039 28.1291 15.2414 28.1291 15.758 27.6126L20.5 22.8705L25.2421 27.6126C25.7586 28.1291 26.5961 28.1291 27.1126 27.6126C27.6291 27.0961 27.6291 26.2586 27.1126 25.7421L22.3705 21L27.1126 16.258C27.6291 15.7415 27.6291 14.904 27.1126 14.3874C26.596 13.8709 25.7586 13.8709 25.2421 14.3874L20.5 19.1294L15.758 14.3874C15.2414 13.8709 14.4039 13.8709 13.8874 14.3874Z"
                  fill="currentColor"
                />
              </svg>
            </div>
          </button>

          <button
            type="submit"
            onClick={handleSubmit}
            disabled={mutation.status === "pending"}
            className="flex items-center justify-center gap-3 px-4 py-3 bg-gradient-to-r from-tanishq-primary-400 to-tanishq-primary-500 rounded-[80px] text-white font-fraunces text-lg font-normal leading-[128%] tracking-[-0.36px] backdrop-blur-[12px] hover:from-tanishq-primary-500 hover:to-tanishq-primary-600 disabled:opacity-50 disabled:cursor-not-allowed transition-all flex-1"
          >
            {mutation.status === "pending" ? "Submitting..." : "Submit"}
            <div className="w-8 h-8 rounded-full bg-tanishq-primary-400 flex items-center justify-center shadow-inner">
              {mutation.status === "pending" ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
              ) : (
                <svg className="w-4 h-4" viewBox="0 0 20 20" fill="none">
                  <path
                    d="M7.5 5L12.5 10L7.5 15"
                    stroke="white"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
              )}
            </div>
          </button>
        </div>
      </div>
    </div>
  );
}
```

## Summary of Frontend Fixes

1. ✅ **Added text sanitization** for mobile input
2. ✅ **Added video validation** before submit
3. ✅ **Added global error display** that user can see
4. ✅ **Improved error handling** with proper error messages
5. ✅ **Added console logging** for debugging
6. ✅ **Clear errors on input change** for better UX

## Next Steps

1. **Create the `lib/api.ts` file** with the correct API implementation (see above)
2. **Update your UserForm component** with the fixes
3. **Test on mobile** after deploying backend changes
4. **Check browser console** for any API errors

The combination of backend + frontend fixes should completely resolve the mobile submission issue!

