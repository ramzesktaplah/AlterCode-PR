package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProgrammingLanguage(
    val id: String,
    val displayName: String,
    val extension: String,
    val starterCode: String
) {
    KOTLIN(
        id = "kotlin",
        displayName = "Kotlin",
        extension = "kt",
        starterCode = """
            class SlidingWindowLimiter(
                private val maxRequests: Int = 10,
                private val windowMs: Long = 1000L
            ) {
                private val timestamps = ArrayDeque<Long>()
                private val mutex = kotlinx.coroutines.sync.Mutex()

                suspend fun allowRequest(): Boolean = mutex.withLock {
                    val now = System.currentTimeMillis()
                    val threshold = now - windowMs
                    while (timestamps.isNotEmpty() && timestamps.first() <= threshold) {
                        timestamps.removeFirst()
                    }
                    if (timestamps.size < maxRequests) {
                        timestamps.addLast(now)
                        true
                    } else {
                        false
                    }
                }
            }
        """.trimIndent()
    ),
    RUST(
        id = "rust",
        displayName = "Rust",
        extension = "rs",
        starterCode = """
            use std::collections::VecDeque;
            use std::time::{Duration, Instant};

            pub struct SlidingWindowLimiter {
                max_requests: usize,
                window: Duration,
                timestamps: VecDeque<Instant>,
            }

            impl SlidingWindowLimiter {
                pub fn new(max_requests: usize, window: Duration) -> Self {
                    Self {
                        max_requests,
                        window,
                        timestamps: VecDeque::new(),
                    }
                }

                pub fn allow_request(&mut self) -> bool {
                    let now = Instant::now();
                    while let Some(&front) = self.timestamps.front() {
                        if now.duration_since(front) > self.window {
                            self.timestamps.pop_front();
                        } else {
                            break;
                        }
                    }
                    if self.timestamps.len() < self.max_requests {
                        self.timestamps.push_back(now);
                        true
                    } else {
                        false
                    }
                }
            }
        """.trimIndent()
    ),
    TYPESCRIPT(
        id = "typescript",
        displayName = "TypeScript",
        extension = "ts",
        starterCode = """
            export class SlidingWindowLimiter {
              private timestamps: number[] = [];

              constructor(
                private readonly maxRequests: number = 10,
                private readonly windowMs: number = 1000
              ) {}

              allowRequest(): boolean {
                const now = Date.now();
                const threshold = now - this.windowMs;
                this.timestamps = this.timestamps.filter((ts) => ts > threshold);
                if (this.timestamps.length < this.maxRequests) {
                  this.timestamps.push(now);
                  return true;
                }
                return false;
              }
            }
        """.trimIndent()
    ),
    PYTHON(
        id = "python",
        displayName = "Python",
        extension = "py",
        starterCode = """
            import time
            from collections import deque

            class SlidingWindowLimiter:
                def __init__(self, max_requests: int = 10, window_secs: float = 1.0):
                    self.max_requests = max_requests
                    self.window_secs = window_secs
                    self.timestamps = deque()

                def allow_request(self) -> bool:
                    now = time.monotonic()
                    threshold = now - self.window_secs
                    while self.timestamps and self.timestamps[0] <= threshold:
                        self.timestamps.popleft()
                    if len(self.timestamps) < self.max_requests:
                        self.timestamps.append(now)
                        return True
                    return False
        """.trimIndent()
    ),
    GO(
        id = "go",
        displayName = "Go",
        extension = "go",
        starterCode = """
            package limiter

            import (
            	"sync"
            	"time"
            )

            type SlidingWindowLimiter struct {
            	mu          sync.Mutex
            	maxRequests int
            	window      time.Duration
            	timestamps  []time.Time
            }

            func New(maxRequests int, window time.Duration) *SlidingWindowLimiter {
            	return &SlidingWindowLimiter{
            		maxRequests: maxRequests,
            		window:      window,
            		timestamps:  make([]time.Time, 0),
            	}
            }

            func (l *SlidingWindowLimiter) Allow() bool {
            	l.mu.Lock()
            	defer l.mu.Unlock()

            	now := time.Now()
            	threshold := now.Add(-l.window)

            	validIdx := 0
            	for i, ts := range l.timestamps {
            		if ts.After(threshold) {
            			validIdx = i
            			break
            		}
            	}
            	l.timestamps = l.timestamps[validIdx:]

            	if len(l.timestamps) < l.maxRequests {
            		l.timestamps = append(l.timestamps, now)
            		return true
            	}
            	return false
            }
        """.trimIndent()
    ),
    CPP(
        id = "cpp",
        displayName = "C++",
        extension = "cpp",
        starterCode = """
            #include <chrono>
            #include <deque>
            #include <mutex>

            class SlidingWindowLimiter {
            public:
                SlidingWindowLimiter(size_t maxRequests, std::chrono::milliseconds window)
                    : maxRequests_(maxRequests), window_(window) {}

                bool allowRequest() {
                    std::lock_guard<std::mutex> lock(mutex_);
                    auto now = std::chrono::steady_clock::now();
                    auto threshold = now - window_;

                    while (!timestamps_.empty() && timestamps_.front() <= threshold) {
                        timestamps_.pop_front();
                    }

                    if (timestamps_.size() < maxRequests_) {
                        timestamps_.push_back(now);
                        return true;
                    }
                    return false;
                }

            private:
                size_t maxRequests_;
                std::chrono::milliseconds window_;
                std::deque<std::chrono::steady_clock::time_point> timestamps_;
                std::mutex mutex_;
            };
        """.trimIndent()
    ),
    SWIFT(
        id = "swift",
        displayName = "Swift",
        extension = "swift",
        starterCode = """
            import Foundation

            actor SlidingWindowLimiter {
                private let maxRequests: Int
                private let window: TimeInterval
                private var timestamps: [Date] = []

                init(maxRequests: Int = 10, window: TimeInterval = 1.0) {
                    self.maxRequests = maxRequests
                    self.window = window
                }

                func allowRequest() -> Bool {
                    let now = Date()
                    let threshold = now.addingTimeInterval(-window)
                    timestamps.removeAll { ${'$'}0 <= threshold }
                    if timestamps.count < maxRequests {
                        timestamps.append(now)
                        return true
                    }
                    return false
                }
            }
        """.trimIndent()
    );

    companion object {
        fun fromId(id: String): ProgrammingLanguage =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: KOTLIN
    }
}

enum class AiActionType(val id: String, val label: String, val accentColor: Color) {
    CONVERT("convert", "Convert", Color(0xFF1D72FE)),
    REFACTOR("refactor", "Refactor", Color(0xFF06B6D4)),
    FIX("fix", "Fix Bugs", Color(0xFF10B981)),
    EXPLAIN("explain", "Explain", Color(0xFF8B5CF6));

    companion object {
        fun fromId(id: String): AiActionType =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: CONVERT
    }
}

data class DiagnosticItem(
    val severity: String,
    val line: Int,
    val message: String,
    val suggestion: String? = null
)

data class AiExplanation(
    val title: String,
    val body: String,
    val keyTakeaway: String
)

data class AiTransformationResult(
    val transformedCode: String,
    val summary: String,
    val diagnostics: List<DiagnosticItem>,
    val explanation: AiExplanation?,
    val routedProvider: String,
    val complexityBefore: String,
    val complexityAfter: String
)

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val sourceCode: String,
    val outputCode: String,
    val summary: String,
    val diagnosticsText: String,
    val explanationText: String,
    val actionType: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val complexityBefore: String,
    val complexityAfter: String,
    val routedProvider: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String,
    val primaryLanguage: String,
    val isEncryptedVault: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "project_files")
data class ProjectFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val projectId: Long,
    val fileName: String,
    val language: String,
    val content: String,
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "usage_records")
data class UsageRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String,
    val languagePair: String,
    val routedProvider: String,
    val deviceFingerprintHash: String
)

data class QuotaStatus(
    val burstUsed: Int,
    val burstLimit: Int,
    val hourlyUsed: Int,
    val hourlyLimit: Int,
    val dailyUsed: Int,
    val dailyLimit: Int,
    val bonusBurstGranted: Int,
    val deviceFingerprint: String,
    val encryptionBadge: String,
    val isAllowed: Boolean
)
