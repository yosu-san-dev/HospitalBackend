package com.ryr.hospital.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "doctors")
public class Doctor {
    
    @Id
    private String id; // otomatik id atanacak

    private String name;
    private String branch;
    private int age;

    public Doctor() {}

    public Doctor(String name, String branch, int age) {
        this.name = name;
        this.branch = branch;
        this.age = age;
    }

    // Get'ler ve Set'ler
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    /* 
    {
        doktorListesi = new ArrayList<>();
    

        
       // Kardiyoloji (3)
        doktorListesi.add(new Doctor("Ahmet Yilmaz", "Kardiyoloji",27));
        doktorListesi.add(new Doctor("Selin Karaca", "Kardiyoloji",45));
        doktorListesi.add(new Doctor("Murat Aydin", "Kardiyoloji",35));

        // KBB (3)
        doktorListesi.add(new Doctor("Mehmet Kaya", "KBB",40));
        doktorListesi.add(new Doctor("Hakan Öztürk", "KBB",50));
        doktorListesi.add(new Doctor("Büsra Aksoy", "KBB", 30));

        // Göz Hastalıkları (3)
        doktorListesi.add(new Doctor("Elif Demir", "Göz Hastalıkları", 56));
        doktorListesi.add(new Doctor("Yusuf Fatthi", "Göz Hastalıkları",37));
        doktorListesi.add(new Doctor("Seda Korkmaz", "Göz Hastalıkları",29));

        // Kadın Doğum (3)
        doktorListesi.add(new Doctor("Ahmet Akkuş", "Kadın Doğum",42));
        doktorListesi.add(new Doctor("Derya Uslu", "Kadın Doğum",32));
        doktorListesi.add(new Doctor("Nuran Polat", "Kadın Doğum",58));

        // Diş (3)
        doktorListesi.add(new Doctor("Selim Arslan", "Diş",35));
        doktorListesi.add(new Doctor("Nihat Bilgin", "Diş",48));
        doktorListesi.add(new Doctor("Recep Dogan", "Diş",44));

        // Çocuk Hastalıkları (3)
        doktorListesi.add(new Doctor("Dogukan Sasi", "Çocuk Hastalıkları",31));
        doktorListesi.add(new Doctor("Yusuf Sert", "Çocuk Hastalıkları",40));
        doktorListesi.add(new Doctor("Rabia Emirhan", "Çocuk Hastalıkları",31));
    }
    
    
    public String getDrName()
    {
        return DrName;
    }
    
    public void puanVerme(Scanner scanner) {
        // Önce listeyi gösterelim ki kullanıcı kimi seçeceğini bilsin
        doktorlariGoster(doktorListesi);
        kisisayisi++;
    
        while (true) {
            System.out.println("Puanlamak istediğiniz doktorun ismini giriniz (Çıkış için 'iptal' yazabilirsiniz):");
            String doktorDrName = scanner.nextLine();
    
            // Kullanıcı çıkmak isterse döngüyü kırıyoruz
            if (doktorDrName.equalsIgnoreCase("iptal")) {
                System.out.println("İşlem iptal edildi.");
                break;
            }
    
            boolean doktorBulundu = false;
    
            // Doktoru listede arıyoruz
            for (Doctor d : doktorListesi) {
                // equalsIgnoreCase ile büyük/küçük harf duyarlılığını kaldırıyoruz
                if (d.getDrName().equalsIgnoreCase(doktorDrName)) {
                    doktorBulundu = true;
                    
                    // Doktor bulunduysa puan sorma aşamasına geçiyoruz
                    System.out.println("Lütfen 0-5 arası bir puan giriniz:");
                    
                    // Burada hata yönetimi (try-catch) eklenebilir ama şimdilik basit tutalım
                    if (scanner.hasNextInt()) {
                        int puan = scanner.nextInt();
                        scanner.nextLine(); // Buffer temizleme (Enter tuşu hatasını önler)
    
                        if (puan >= 0 && puan <= 5) {
                            // Puan hesaplama mantığı (Mevcut mantığını korudum)
                            d.DoktorPuan = (d.DoktorPuan + puan) / kisisayisi;
                            
                            System.out.println("Yeni Ortalama Puan: " + d.DoktorPuan);
                            System.out.println("✅ Puan başarıyla eklendi.");
                            return; // İşlem bitti, metottan tamamen çıkıyoruz
                        } else {
                            System.out.println("⚠️ Lütfen 0 ile 5 arasında geçerli bir rakam giriniz!");
                        }
                    } else {
                        System.out.println("⚠️ Hatalı giriş! Lütfen sayı giriniz.");
                        scanner.nextLine(); // Hatalı girdiyi temizle
                    }
                    break; // Doktor bulundu döngüsünden (for) çık, while döngüsü başa dönecek veya return ile bitecek
                }
            }
    
            if (!doktorBulundu) {
                System.out.println("❌ Doktor bulunamadı! Lütfen ismi kontrol edip tekrar deneyiniz.");
            }
        }
    }
    
    
    
    public String getBranş()
    {
        return branch;
    }
    
    public void doktorlariGoster(ArrayList<Doctor> liste) {
        System.out.println("\n--- DOKTOR LİSTESİ ---");
        
        if (liste.isEmpty()) {
            System.out.println("Sistemde henüz kayıtlı doktor yok.");
        } else {
            for (Doctor d : liste) {
                System.out.println("🩺 " + d.DrName + " \t| Uzmanlık: " + d.branch + "\t| Yaş:" + d.age);
            }
        }
    }
    */
}
