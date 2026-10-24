package com.example.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.R as AppR

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
package com.ai.altercode.rate

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SlidingWindowLimiter(
    private val maxBurstPerMinute: Int = 60,
    private val maxSustainedPerHour: Int = 1000
) {
    private val mutex = Mutex()
    private val timestamps = ArrayDeque<Long>()

    suspend fun tryAcquire(nowMs: Long = System.currentTimeMillis()): Boolean {
        val oneHourAgo = nowMs - 3_600_000L
        val oneMinuteAgo = nowMs - 60_000L
        return mutex.withLock {
            while (timestamps.isNotEmpty() && timestamps.first() < oneHourAgo) {
                timestamps.removeFirst()
            }
            val minuteCount = timestamps.count { it >= oneMinuteAgo }
            if (minuteCount < maxBurstPerMinute && timestamps.size < maxSustainedPerHour) {
                timestamps.addLast(nowMs)
                true
            } else {
                false
            }
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
    ),
    JAVA(
        id = "java",
        displayName = "Java",
        extension = "java",
        starterCode = """
            package com.example.limiter;

            import java.time.Instant;
            import java.util.ArrayDeque;
            import java.util.Deque;

            public class SlidingWindowLimiter {
                private final int maxRequests;
                private final long windowMillis;
                private final Deque<Instant> timestamps = new ArrayDeque<>();

                public SlidingWindowLimiter(int maxRequests, long windowMillis) {
                    this.maxRequests = maxRequests;
                    this.windowMillis = windowMillis;
                }

                public synchronized boolean allowRequest() {
                    Instant now = Instant.now();
                    Instant threshold = now.minusMillis(windowMillis);
                    while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(threshold)) {
                        timestamps.pollFirst();
                    }
                    if (timestamps.size() < maxRequests) {
                        timestamps.addLast(now);
                        return true;
                    }
                    return false;
                }
            }
        """.trimIndent()
    ),
    JAVASCRIPT(
        id = "javascript",
        displayName = "JavaScript",
        extension = "js",
        starterCode = """
            export class SlidingWindowLimiter {
              constructor(maxRequests = 10, windowMs = 1000) {
                this.maxRequests = maxRequests;
                this.windowMs = windowMs;
                this.timestamps = [];
              }

              allowRequest() {
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
    CSHARP(
        id = "csharp",
        displayName = "C#",
        extension = "cs",
        starterCode = """
            using System;
            using System.Collections.Generic;

            public class SlidingWindowLimiter
            {
                private readonly int _maxRequests;
                private readonly TimeSpan _window;
                private readonly Queue<DateTime> _timestamps = new();
                private readonly object _lock = new();

                public SlidingWindowLimiter(int maxRequests = 10, int windowSeconds = 1)
                {
                    _maxRequests = maxRequests;
                    _window = TimeSpan.FromSeconds(windowSeconds);
                }

                public bool AllowRequest()
                {
                    lock (_lock)
                    {
                        var now = DateTime.UtcNow;
                        var threshold = now - _window;
                        while (_timestamps.Count > 0 && _timestamps.Peek() <= threshold)
                        {
                            _timestamps.Dequeue();
                        }
                        if (_timestamps.Count < _maxRequests)
                        {
                            _timestamps.Enqueue(now);
                            return true;
                        }
                        return false;
                    }
                }
            }
        """.trimIndent()
    ),
    CPLUS(
        id = "cplus",
        displayName = "C+",
        extension = "cp",
        starterCode = """
            // C+ Optimized Sliding Window Limiter
            struct SlidingWindowLimiter {
                int maxRequests;
                double windowSec;
                double timestamps[128];
                int count;

                void init(int maxReq, double win) {
                    maxRequests = maxReq;
                    windowSec = win;
                    count = 0;
                }

                bool allowRequest(double now) {
                    double threshold = now - windowSec;
                    int valid = 0;
                    for (int i = 0; i < count; i++) {
                        if (timestamps[i] > threshold) {
                            timestamps[valid++] = timestamps[i];
                        }
                    }
                    count = valid;
                    if (count < maxRequests) {
                        timestamps[count++] = now;
                        return true;
                    }
                    return false;
                }
            };
        """.trimIndent()
    ),
    HTML(
        id = "html",
        displayName = "HTML",
        extension = "html",
        starterCode = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <title>Rate Limiter Dashboard</title>
              <style>
                body { font-family: sans-serif; background: #0f172a; color: #f8fafc; padding: 2rem; }
                .card { background: #1e293b; padding: 1.5rem; border-radius: 12px; max-width: 480px; }
                .badge { color: #38bdf8; font-weight: bold; margin-bottom: 1rem; }
                button { background: #2563eb; color: #fff; border: 0; padding: 10px 18px; border-radius: 8px; cursor: pointer; }
              </style>
            </head>
            <body>
              <main class="card">
                <h2>Sliding Window Monitor</h2>
                <div id="status" class="badge">Permits Remaining: 10 / 10</div>
                <button onclick="alert('Permit acquired')">Acquire Permit</button>
              </main>
            </body>
            </html>
        """.trimIndent()
    ),
    PHP(
        id = "php",
        displayName = "PHP",
        extension = "php",
        starterCode = """
            <?php

            class SlidingWindowLimiter {
                private int ${'$'}maxRequests;
                private float ${'$'}windowSec;
                private array ${'$'}timestamps = [];

                public function __construct(int ${'$'}maxRequests = 10, float ${'$'}windowSec = 1.0) {
                    ${'$'}this->maxRequests = ${'$'}maxRequests;
                    ${'$'}this->windowSec = ${'$'}windowSec;
                }

                public function allowRequest(): bool {
                    ${'$'}now = microtime(true);
                    ${'$'}threshold = ${'$'}now - ${'$'}this->windowSec;
                    ${'$'}this->timestamps = array_values(
                        array_filter(${'$'}this->timestamps, fn(${'$'}ts) => ${'$'}ts > ${'$'}threshold)
                    );
                    if (count(${'$'}this->timestamps) < ${'$'}this->maxRequests) {
                        ${'$'}this->timestamps[] = ${'$'}now;
                        return true;
                    }
                    return false;
                }
            }
        """.trimIndent()
    ),
    SQL(
        id = "sql",
        displayName = "SQL",
        extension = "sql",
        starterCode = """
            -- Sliding Window Limiter Audit Table & Window Query
            CREATE TABLE api_requests (
                id SERIAL PRIMARY KEY,
                client_ip VARCHAR(45) NOT NULL,
                requested_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
            );

            SELECT client_ip,
                   COUNT(*) OVER (
                       PARTITION BY client_ip
                       ORDER BY requested_at
                       RANGE BETWEEN INTERVAL '1 minute' PRECEDING AND CURRENT ROW
                   ) AS window_burst_count
            FROM api_requests;
        """.trimIndent()
    ),
    RUBY(
        id = "ruby",
        displayName = "Ruby",
        extension = "rb",
        starterCode = """
            class SlidingWindowLimiter
              def initialize(max_requests: 10, window_secs: 1.0)
                @max_requests = max_requests
                @window_secs = window_secs
                @timestamps = []
                @mutex = Mutex.new
              end

              def allow_request?
                @mutex.synchronize do
                  now = Process.clock_gettime(Process::CLOCK_MONOTONIC)
                  threshold = now - @window_secs
                  @timestamps.reject! { |ts| ts <= threshold }
                  if @timestamps.size < @max_requests
                    @timestamps << now
                    true
                  else
                    false
                  end
                end
              end
            end
        """.trimIndent()
    ),
    DART(
        id = "dart",
        displayName = "Dart",
        extension = "dart",
        starterCode = """
            import 'dart:collection';

            class SlidingWindowLimiter {
              final int maxRequests;
              final Duration window;
              final Queue<DateTime> _timestamps = Queue<DateTime>();

              SlidingWindowLimiter({
                this.maxRequests = 10,
                this.window = const Duration(seconds: 1),
              });

              bool allowRequest() {
                final now = DateTime.now();
                final threshold = now.subtract(window);
                while (_timestamps.isNotEmpty && _timestamps.first.isBefore(threshold)) {
                  _timestamps.removeFirst();
                }
                if (_timestamps.length < maxRequests) {
                  _timestamps.addLast(now);
                  return true;
                }
                return false;
              }
            }
        """.trimIndent()
    ),
    C(
        id = "c",
        displayName = "C",
        extension = "c",
        starterCode = """
            #include <stdio.h>
            #include <stdbool.h>
            #include <time.h>

            #define MAX_TIMESTAMPS 128

            typedef struct {
                int max_requests;
                double window_seconds;
                double timestamps[MAX_TIMESTAMPS];
                int count;
            } SlidingWindowLimiter;

            bool allow_request(SlidingWindowLimiter *limiter, double now) {
                double threshold = now - limiter->window_seconds;
                int write_idx = 0;
                for (int i = 0; i < limiter->count; i++) {
                    if (limiter->timestamps[i] > threshold) {
                        limiter->timestamps[write_idx++] = limiter->timestamps[i];
                    }
                }
                limiter->count = write_idx;
                if (limiter->count < limiter->max_requests) {
                    limiter->timestamps[limiter->count++] = now;
                    return true;
                }
                return false;
            }
        """.trimIndent()
    ),
    LUA(
        id = "lua",
        displayName = "Lua",
        extension = "lua",
        starterCode = """
            local SlidingWindowLimiter = {}
            SlidingWindowLimiter.__index = SlidingWindowLimiter

            function SlidingWindowLimiter.new(max_requests, window_secs)
                local self = setmetatable({}, SlidingWindowLimiter)
                self.max_requests = max_requests or 10
                self.window_secs = window_secs or 1.0
                self.timestamps = {}
                return self
            end

            function SlidingWindowLimiter:allow_request()
                local now = os.clock()
                local threshold = now - self.window_secs
                local filtered = {}
                for _, ts in ipairs(self.timestamps) do
                    if ts > threshold then
                        table.insert(filtered, ts)
                    end
                end
                self.timestamps = filtered
                if #self.timestamps < self.max_requests then
                    table.insert(self.timestamps, now)
                    return true
                end
                return false
            end

            return SlidingWindowLimiter
        """.trimIndent()
    ),
    HASKELL(
        id = "haskell",
        displayName = "Haskell",
        extension = "hs",
        starterCode = """
            module SlidingWindowLimiter (allowRequest) where

            import Data.Time.Clock

            data Limiter = Limiter
              { maxRequests :: Int
              , windowDiff  :: NominalDiffTime
              , timestamps  :: [UTCTime]
              }

            allowRequest :: UTCTime -> Limiter -> (Bool, Limiter)
            allowRequest now limiter =
              let threshold = addUTCTime (- windowDiff limiter) now
                  active    = filter (> threshold) (timestamps limiter)
              in if length active < maxRequests limiter
                   then (True, limiter { timestamps = now : active })
                   else (False, limiter { timestamps = active })
        """.trimIndent()
    ),
    PERL(
        id = "perl",
        displayName = "Perl",
        extension = "pl",
        starterCode = """
            package SlidingWindowLimiter;
            use strict;
            use warnings;
            use Time::HiRes qw(time);

            sub new {
                my (${'$'}class, %args) = @_;
                return bless {
                    max_requests => ${'$'}args{max_requests} || 10,
                    window_secs  => ${'$'}args{window_secs}  || 1.0,
                    timestamps   => [],
                }, ${'$'}class;
            }

            sub allow_request {
                my (${'$'}self) = @_;
                my ${'$'}now = time();
                my ${'$'}threshold = ${'$'}now - ${'$'}self->{window_secs};
                @{${'$'}self->{timestamps}} = grep { ${'$'}_ > ${'$'}threshold } @{${'$'}self->{timestamps}};
                if (@{${'$'}self->{timestamps}} < ${'$'}self->{max_requests}) {
                    push @{${'$'}self->{timestamps}}, ${'$'}now;
                    return 1;
                }
                return 0;
            }

            1;
        """.trimIndent()
    ),
    OBJECTIVE_C(
        id = "objectivec",
        displayName = "Objective-C",
        extension = "m",
        starterCode = """
            #import <Foundation/Foundation.h>

            @interface SlidingWindowLimiter : NSObject
            @property (nonatomic, assign) NSInteger maxRequests;
            @property (nonatomic, assign) NSTimeInterval windowSeconds;
            @property (nonatomic, strong) NSMutableArray<NSDate *> *timestamps;
            - (BOOL)allowRequest;
            @end

            @implementation SlidingWindowLimiter
            - (instancetype)init {
                if (self = [super init]) {
                    _maxRequests = 10;
                    _windowSeconds = 1.0;
                    _timestamps = [NSMutableArray array];
                }
                return self;
            }
            - (BOOL)allowRequest {
                @synchronized (self) {
                    NSDate *now = [NSDate date];
                    NSDate *threshold = [now dateByAddingTimeInterval:-self.windowSeconds];
                    [self.timestamps filterUsingPredicate:[NSPredicate predicateWithFormat:@"SELF > %@", threshold]];
                    if (self.timestamps.count < self.maxRequests) {
                        [self.timestamps addObject:now];
                        return YES;
                    }
                    return NO;
                }
            }
            @end
        """.trimIndent()
    ),
    R(
        id = "r",
        displayName = "R",
        extension = "r",
        starterCode = """
            SlidingWindowLimiter <- setRefClass(
              "SlidingWindowLimiter",
              fields = list(
                max_requests = "numeric",
                window_secs = "numeric",
                timestamps = "numeric"
              ),
              methods = list(
                initialize = function(max_requests = 10, window_secs = 1.0) {
                  .self${'$'}max_requests <- max_requests
                  .self${'$'}window_secs <- window_secs
                  .self${'$'}timestamps <- numeric(0)
                },
                allow_request = function() {
                  now <- as.numeric(Sys.time())
                  threshold <- now - .self${'$'}window_secs
                  .self${'$'}timestamps <- .self${'$'}timestamps[.self${'$'}timestamps > threshold]
                  if (length(.self${'$'}timestamps) < .self${'$'}max_requests) {
                    .self${'$'}timestamps <- c(.self${'$'}timestamps, now)
                    return(TRUE)
                  }
                  return(FALSE)
                }
              )
            )
        """.trimIndent()
    );

    val iconRes: Int
        @DrawableRes
        get() = when (this) {
            KOTLIN -> AppR.drawable.ic_lang_kotlin
            RUST -> AppR.drawable.ic_lang_rust
            TYPESCRIPT -> AppR.drawable.ic_lang_typescript
            PYTHON -> AppR.drawable.ic_lang_python
            GO -> AppR.drawable.ic_lang_go
            CPP -> AppR.drawable.ic_lang_cpp
            SWIFT -> AppR.drawable.ic_lang_swift
            JAVA -> AppR.drawable.ic_lang_java
            JAVASCRIPT -> AppR.drawable.ic_lang_javascript
            CSHARP -> AppR.drawable.ic_lang_csharp
            CPLUS -> AppR.drawable.ic_lang_cplus
            HTML -> AppR.drawable.ic_lang_html
            PHP -> AppR.drawable.ic_lang_php
            SQL -> AppR.drawable.ic_lang_sql
            RUBY -> AppR.drawable.ic_lang_ruby
            DART -> AppR.drawable.ic_lang_dart
            C -> AppR.drawable.ic_lang_c
            LUA -> AppR.drawable.ic_lang_lua
            HASKELL -> AppR.drawable.ic_lang_haskell
            PERL -> AppR.drawable.ic_lang_perl
            OBJECTIVE_C -> AppR.drawable.ic_lang_objectivec
            R -> AppR.drawable.ic_lang_r
        }

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
