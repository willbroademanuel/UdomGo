package com.example.udomgo;

import org.junit.Test;
import static org.junit.Assert.*;

public class DatabaseHelperModelTest {

    @Test
    public void testCourseItemCreation() {
        DatabaseHelper.CourseItem item = new DatabaseHelper.CourseItem(
                1L, "BSc Software Engineering", 1, 1,
                "CP 111", "Introduction to Computer Programming", 9.0,
                "Core", "Core Software"
        );
        assertEquals(1L, item.id);
        assertEquals("BSc Software Engineering", item.programme);
        assertEquals(1, item.yearOfStudy);
        assertEquals(1, item.semester);
        assertEquals("CP 111", item.courseCode);
        assertEquals("Introduction to Computer Programming", item.courseTitle);
        assertEquals(9.0, item.credits, 0.001);
        assertEquals("Core", item.courseStatus);
        assertEquals("Core Software", item.category);
    }

    @Test
    public void testTimetableItemSessionType() {
        DatabaseHelper.TimetableItem item = new DatabaseHelper.TimetableItem(
                10L, "BSc Software Engineering", 1, 2, "Monday",
                "16:00", "18:00", "CN 121", "Introduction to Computer Networking",
                "Practical", "NET_LAB", "Dr. Mohamed Mjahid"
        );
        assertEquals(10L, item.id);
        assertEquals("Practical", item.sessionType);
        assertEquals(2, item.semester);
        assertEquals("NET_LAB", item.venue);

        // Test backward compatibility constructor
        DatabaseHelper.TimetableItem oldItem = new DatabaseHelper.TimetableItem(
                11L, "BSc Software Engineering", 1, "Monday",
                "08:00", "10:00", "CS 111", "Intro to Programming",
                "LR A", "Dr. Mwamba"
        );
        assertEquals("Theory", oldItem.sessionType);
        assertEquals(1, oldItem.semester);
    }

    @Test
    public void testCalendarEventItemMultiYear() {
        DatabaseHelper.CalendarEventItem event = new DatabaseHelper.CalendarEventItem(
                5L, "2025/2026", "Semester II",
                "University Examinations (UE) - Semester 2", "EXAM",
                "2026-07-20", "2026-07-31",
                "Final degree University Examinations (UE) for Semester 2"
        );
        assertEquals(5L, event.id);
        assertEquals("2025/2026", event.academicYear);
        assertEquals("Semester II", event.semester);
        assertEquals("EXAM", event.category);

        // Test backward compatibility constructor
        DatabaseHelper.CalendarEventItem oldEvent = new DatabaseHelper.CalendarEventItem(
                6L, "Independence Day", "HOLIDAY",
                "2026-12-09", "2026-12-09", "Tanzania Independence Day"
        );
        assertEquals("2025/2026", oldEvent.academicYear);
        assertEquals("Semester I", oldEvent.semester);
    }

    @Test
    public void testFriendlyDateFormatting() {
        assertEquals("09 Mar 2026", DatabaseHelper.formatFriendlyDate("2026-03-09"));
        assertEquals("30 Jun 2025", DatabaseHelper.formatFriendlyDate("2025-06-30"));
        assertEquals("", DatabaseHelper.formatFriendlyDate(null));
        assertEquals("", DatabaseHelper.formatFriendlyDate(""));

        // Date range same year
        assertEquals("09 Mar – 20 Mar 2026", DatabaseHelper.formatFriendlyDateRange("2026-03-09", "2026-03-20"));
        assertEquals("30 Jun – 11 Jul 2025", DatabaseHelper.formatFriendlyDateRange("2025-06-30", "2025-07-11"));

        // Single date or same start/end
        assertEquals("14 Oct 2024", DatabaseHelper.formatFriendlyDateRange("2024-10-14", "2024-10-14"));
        assertEquals("09 Mar 2026", DatabaseHelper.formatFriendlyDateRange("2026-03-09", ""));

        // Cross year date range
        assertEquals("23 Dec 2024 – 03 Jan 2025", DatabaseHelper.formatFriendlyDateRange("2024-12-23", "2025-01-03"));
    }

    @Test
    public void testFaqItemCreation() {
        DatabaseHelper.FaqItem faq = new DatabaseHelper.FaqItem(
                100L, "heslb", "loan,boom",
                "How do HESLB loans work at UDOM?",
                "Detailed loan disbursement info...",
                "ALL"
        );
        assertEquals(100L, faq.id);
        assertEquals("heslb", faq.category);
        assertEquals("loan,boom", faq.keywords);
        assertEquals("How do HESLB loans work at UDOM?", faq.question);
        assertEquals("Detailed loan disbursement info...", faq.answer);
        assertEquals("ALL", faq.collegeSpecific);
    }

    @Test
    public void testConstants() {
        assertEquals("courses", DatabaseHelper.TABLE_COURSES);
        assertEquals("session_type", DatabaseHelper.COLUMN_TIME_SESSION);
        assertEquals("academic_year", DatabaseHelper.COLUMN_CAL_YEAR);
        assertEquals("semester", DatabaseHelper.COLUMN_CAL_SEMESTER);
        assertEquals(8, DatabaseHelper.DATABASE_VERSION);
    }
}
