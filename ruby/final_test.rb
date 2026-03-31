require_relative 'sahih_al_bukhari'

begin
  b = SahihAlBukhari::Bukhari.new
  puts "SUCCESS: Ruby gem working!"
  puts "Total hadiths: #{b.length}"
  puts "Total chapters: #{b.chapters.length}"
  puts "First hadith: #{b.get(1).english[0..50]}..."
rescue => e
  puts "ERROR: #{e.message}"
end
