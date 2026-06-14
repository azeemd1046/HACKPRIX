package com.example.data

import android.content.Context
import com.example.data.NetworkClients
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

// Skill model for the catalog
data class Skill(
    val name: String,
    val category: String, // Tech, Creative, Business, Finance, Communication
    val difficulty: String, // Beginner, Intermediate, Advanced
    val estimatedTime: String, // e.g., "12 weeks"
    val incomePotential: String // e.g., "$95k/yr"
)

// Resource model
data class ResourceItem(
    val name: String,
    val careerPath: String, // E.g. "Cybersecurity", "Data Science", etc.
    val category: String, // E.g. "YouTube Channels", "Free Courses", "Paid Courses", "Certifications", "Practice Platforms", "Projects"
    val difficulty: String, // E.g. "Beginner", "Intermediate", "Advanced", or "N/A"
    val provider: String,
    val description: String,
    val cost: String, // Free, Paid, Free/Paid
    val url: String
)

class SkillRepository(context: Context) {
    private val appDatabase = AppDatabase.getDatabase(context)
    private val appDao = appDatabase.appDao()

    val latestProfile: Flow<ProfileEntity?> = appDao.getLatestProfile()
    val roadmapTasks: Flow<List<RoadmapTaskEntity>> = appDao.getRoadmapTasks()

    // ------------------------------------------------------------------------
    // PREDEFINED DATA DIRECTORIES
    // ------------------------------------------------------------------------

    val skillLibraryList = listOf(
        // Tech
        Skill("Full Stack Web Dev", "Tech", "Intermediate", "16 Weeks", "$110,000/yr"),
        Skill("Android App Development", "Tech", "Intermediate", "12 Weeks", "$115,000/yr"),
        Skill("Cybersecurity Fundamentals", "Tech", "Beginner", "8 Weeks", "$90,000/yr"),
        Skill("Cloud Architecture (AWS)", "Tech", "Advanced", "14 Weeks", "$130,000/yr"),
        Skill("Python & Data Science", "Tech", "Beginner", "10 Weeks", "$105,000/yr"),
        
        // Creative
        Skill("Professional Video Editing", "Creative", "Intermediate", "8 Weeks", "$75,000/yr"),
        Skill("UI/UX Design Systems", "Creative", "Intermediate", "12 Weeks", "$95,000/yr"),
        Skill("Vector Graphic Illustration", "Creative", "Beginner", "6 Weeks", "$60,000/yr"),
        Skill("Content Writing & Copy", "Creative", "Beginner", "4 Weeks", "$55,000/yr"),
        
        // Business
        Skill("SaaS Entrepreneurship", "Business", "Advanced", "16 Weeks", "$150,000/yr"),
        Skill("Freelance Client Acquisition", "Business", "Beginner", "4 Weeks", "$80,000/yr"),
        Skill("SEO & Growth Marketing", "Business", "Intermediate", "8 Weeks", "$70,000/yr"),
        
        // Finance
        Skill("Personal Finance Mastery", "Finance", "Beginner", "3 Weeks", "Wealth Multiplier"),
        Skill("Stock & Index Fund Investing", "Finance", "Intermediate", "5 Weeks", "Passive Income"),
        Skill("Financial Model Analysis", "Finance", "Advanced", "8 Weeks", "$110,000/yr"),

        // Communication
        Skill("High-Stakes Public Speaking", "Communication", "Intermediate", "6 Weeks", "Leadership Accelerator"),
        Skill("Transformational Leadership", "Communication", "Advanced", "8 Weeks", "Manager Core")
    )

    val curatedResources = listOf(
        // ==========================================
        // 1. CYBERSECURITY
        // ==========================================
        ResourceItem("John Hammond", "Cybersecurity", "YouTube Channels", "N/A", "John Hammond", "Detailed threat analysis, CTF walk-throughs, and malware analysis.", "Free", "https://www.youtube.com/@JohnHammond"),
        ResourceItem("NetworkChuck", "Cybersecurity", "YouTube Channels", "N/A", "NetworkChuck", "Exciting, highly interactive networks, hacking, and home lab tutorials.", "Free", "https://www.youtube.com/@NetworkChuck"),
        ResourceItem("PortSwigger Web Security Academy", "Cybersecurity", "Free Courses", "N/A", "PortSwigger", "The industry's gold-standard free interactive web application security labs.", "Free", "https://portswigger.net/web-security"),
        ResourceItem("Federal Virtual Training", "Cybersecurity", "Free Courses", "N/A", "CISA (FedVTE)", "Free online government-vouched cyber courses for public usage.", "Free", "https://fedvte.usalearning.gov/"),
        ResourceItem("Practical Ethical Hacking", "Cybersecurity", "Paid Courses", "N/A", "TCM Security", "Hands-on penetration testing, active directory exploit setups, and real labs.", "Paid", "https://academy.tcm-sec.com/p/practical-ethical-hacking-the-complete-course"),
        ResourceItem("SANS SEC401 Security Essentials", "Cybersecurity", "Paid Courses", "N/A", "SANS Institute", "Deep theoretical training covering key enterprise defense mechanisms.", "Paid", "https://www.sans.org/cyber-security-courses/security-essentials-bootcamp-style/"),
        ResourceItem("CompTIA Security+", "Cybersecurity", "Certifications", "Beginner", "CompTIA", "Validated entry standard covering network defense and threat types.", "Paid", "https://www.comptia.org/certifications/security"),
        ResourceItem("Certified Ethical Hacker (CEH)", "Cybersecurity", "Certifications", "Intermediate", "EC-Council", "Examines critical security vectors, sniffing, and social engineering.", "Paid", "https://www.eccouncil.org/train-certify/certified-ethical-hacker-ceh/"),
        ResourceItem("Offensive Security Certified (OSCP)", "Cybersecurity", "Certifications", "Advanced", "OffSec", "Aggressive, highly rigorous 24-hr hands-on lab and exam.", "Paid", "https://www.offsec.com/courses/pen-200/"),
        ResourceItem("TryHackMe", "Cybersecurity", "Practice Platforms", "N/A", "TryHackMe Team", "Stunning gamified lab tutorials explaining both cyber theory and practice.", "Free/Paid", "https://tryhackme.com/"),
        ResourceItem("HackTheBox", "Cybersecurity", "Practice Platforms", "N/A", "HackTheBox Team", "Advanced, competitive sandbox hacking scenarios for seasoned testers.", "Free/Paid", "https://www.hackthebox.com/"),
        ResourceItem("Home Lab Setup", "Cybersecurity", "Projects", "N/A", "Active Directory Lab", "Build modular virtual networks hosting PfSense routers and AD forests.", "Free", "https://github.com/clarkio/home-lab"),
        ResourceItem("Vulnerability Scanner", "Cybersecurity", "Projects", "N/A", "Python Tool", "Draft a custom fast IP socket pinging and service banner scanner.", "Free", "https://github.com/vulnersCom/nmap-vulners"),

        // ==========================================
        // 2. DATA SCIENCE
        // ==========================================
        ResourceItem("Krish Naik", "Data Science", "YouTube Channels", "N/A", "Krish Naik", "Deep dives into math, algorithms, deployment, and cloud systems.", "Free", "https://www.youtube.com/@krishnaik06"),
        ResourceItem("CampusX", "Data Science", "YouTube Channels", "N/A", "CampusX", "Clear educational pathways for statistics, pandas, and scikit-learn.", "Free", "https://www.youtube.com/@campusx-official"),
        ResourceItem("Google ML Crash Course", "Data Science", "Free Courses", "N/A", "Google Developers", "Fast-paced introduction explaining ML frameworks and calculations.", "Free", "https://developers.google.com/machine-learning/crash-course"),
        ResourceItem("Harvard Data Science (CS109)", "Data Science", "Free Courses", "N/A", "Harvard University", "Rigorous statistics, plotting, modeling, and pandas logic.", "Free", "https://cs109.github.io/2015/"),
        ResourceItem("DataCamp Scholar Tracks", "Data Science", "Paid Courses", "N/A", "DataCamp", "Highly interactive and gamified browser-based coding modules.", "Paid", "https://www.datacamp.com/tracks/data-scientist-with-python"),
        ResourceItem("IBM Data Science Certificate", "Data Science", "Paid Courses", "N/A", "IBM / Coursera", "Structured track teaching SQL, data visualization, and regressors.", "Paid", "https://www.coursera.org/professional-certificates/ibm-data-science"),
        ResourceItem("Google Professional Data Engineer", "Data Science", "Certifications", "Beginner", "Google Cloud", "Evaluates fundamental cloud storage and data pipeline processing.", "Paid", "https://cloud.google.com/learn/certification/data-engineer"),
        ResourceItem("Azure Data Scientist (DP-100)", "Data Science", "Certifications", "Intermediate", "Microsoft Azure", "Tests ability to train, publish, and track models using Azure ML.", "Paid", "https://learn.microsoft.com/credentials/certifications/azure-data-scientist/"),
        ResourceItem("SAS Advanced Analytics Specialist", "Data Science", "Certifications", "Advanced", "SAS Institute", "Advanced analysis focusing on predictions, timeseries, and clusters.", "Paid", "https://www.sas.com/en_us/certification/credentials/advanced-analytics/advanced-analytics-professional.html"),
        ResourceItem("Kaggle Marketplace", "Data Science", "Practice Platforms", "N/A", "Kaggle", "Test custom feature tuning against live corporate competitions.", "Free", "https://www.kaggle.com/"),
        ResourceItem("Customer Churn Prediction", "Data Science", "Projects", "N/A", "XGBoost System", "Synthesize user features to model cancel risks via custom plots.", "Free", "https://github.com/topics/customer-churn-prediction"),
        ResourceItem("House Price Regression", "Data Science", "Projects", "N/A", "Regression Model", "Train multi-variable models estimating valuation weights of structures.", "Free", "https://github.com/topics/house-price-prediction"),

        // ==========================================
        // 3. FULL STACK DEVELOPMENT
        // ==========================================
        ResourceItem("Dave Gray", "Full Stack Development", "YouTube Channels", "N/A", "Dave Gray Tech", "Premium instructional modules teaching React, Node, Express, and Mongo.", "Free", "https://www.youtube.com/@DaveGrayTeachesCode"),
        ResourceItem("Jack Herrington", "Full Stack Development", "YouTube Channels", "N/A", "Jack Herrington", "Elite, highly structured lessons covering state patterns and modular UI.", "Free", "https://www.youtube.com/@jherr"),
        ResourceItem("FullStackOpen", "Full Stack Development", "Free Courses", "N/A", "Univ. of Helsinki", "Learn modern single-page apps, GraphQL, Docker, and CI/CD.", "Free", "https://fullstackopen.com/en/"),
        ResourceItem("The Odin Project", "Full Stack Development", "Free Courses", "N/A", "Odin Team", "Strictly-designed project paths helping compile massive portfolios.", "Free", "https://www.theodinproject.com/"),
        ResourceItem("Zero To Mastery: Full-Stack", "Full Stack Development", "Paid Courses", "N/A", "ZTM Academy", "Extensive roadmap teaching React, NodeJS, Postgres, and deployment.", "Paid", "https://academy.zerotomastery.io/p/complete-web-developer-zero-to-mastery"),
        ResourceItem("Ultimate React & Node", "Full Stack Development", "Paid Courses", "N/A", "Udemy Academy", "Build full database-driven apps with user authentication.", "Paid", "https://www.udemy.com/"),
        ResourceItem("freeCodeCamp Certificate", "Full Stack Development", "Certifications", "Beginner", "freeCodeCamp", "Verify 1200 hours completing complex client-safe web structures.", "Free", "https://www.freecodecamp.org/"),
        ResourceItem("AWS Certified Developer Associate", "Full Stack Development", "Certifications", "Intermediate", "Amazon AWS", "Certifies serverless execution, API creation, and security configs.", "Paid", "https://aws.amazon.com/certification/certified-developer-associate/"),
        ResourceItem("Meta Full-Stack Professional", "Full Stack Development", "Certifications", "Advanced", "Meta / Coursera", "Examines production-grade deployment, caching, and testing.", "Paid", "https://www.coursera.org/professional-certificates/meta-full-stack-developer"),
        ResourceItem("Frontend Mentor", "Full Stack Development", "Practice Platforms", "N/A", "Mentor Team", "Practice writing professional HTML, CSS, and JS from Figma cards.", "Free/Paid", "https://www.frontendmentor.io/"),
        ResourceItem("Scrimba Learning", "Full Stack Development", "Practice Platforms", "N/A", "Scrimba Team", "Interactive browser coding built straight inside dynamic web timelines.", "Free/Paid", "https://scrimba.com/"),
        ResourceItem("E-Commerce Storefront", "Full Stack Development", "Projects", "N/A", "Next.js & Stripe", "Assemble complete cart pipelines with Postgres backend syncs.", "Free", "https://github.com/vercel/commerce"),
        ResourceItem("Real-time Chat App", "Full Stack Development", "Projects", "N/A", "Socket.io Engine", "Compile instant messaging apps using custom Node sockets and Redis.", "Free", "https://github.com/hiteshchoudhary/realtime-chat-app"),

        // ==========================================
        // 4. CLOUD COMPUTING
        // ==========================================
        ResourceItem("TechWorld with Nana", "Cloud Computing", "YouTube Channels", "N/A", "Nana Janashia", "Outstanding visualizations explaining Kubernetes, Helm, and DevOps.", "Free", "https://www.youtube.com/@TechWorldWithNana"),
        ResourceItem("FreeCodeCamp Cloud", "Cloud Computing", "YouTube Channels", "N/A", "fCC Team", "Massive, detailed multi-hour bootcamps for AWS, GCP, and Azure.", "Free", "https://www.youtube.com/@freecodecamp"),
        ResourceItem("AWS Training Portal", "Cloud Computing", "Free Courses", "N/A", "Amazon Web Services", "Direct access to hundreds of video lessons written by cloud engineers.", "Free", "https://aws.amazon.com/training/"),
        ResourceItem("Microsoft Learn Paths", "Cloud Computing", "Free Courses", "N/A", "Microsoft", "Self-guided sandbox ecosystems explaining cloud server designs.", "Free", "https://learn.microsoft.com/training/azure/"),
        ResourceItem("Maarek AWS Solutions Architect", "Cloud Computing", "Paid Courses", "N/A", "Udemy / Maarek", "Highest-rated review exploring VPC, EC2, ECS, and Route53 structures.", "Paid", "https://www.udemy.com/user/stephane-maarek/"),
        ResourceItem("Adrian Cantrill AWS Mastery", "Cloud Computing", "Paid Courses", "N/A", "Adrian Cantrill", "Extremely deep, diagram-heavy lessons teaching production setups.", "Paid", "https://learn.cantrill.io/"),
        ResourceItem("AWS Certified Cloud Practitioner", "Cloud Computing", "Certifications", "Beginner", "Amazon AWS", "Validates foundational service understanding and pricing grids.", "Paid", "https://aws.amazon.com/certification/certified-cloud-practitioner/"),
        ResourceItem("AWS Solutions Architect Associate", "Cloud Computing", "Certifications", "Intermediate", "Amazon AWS", "Assess capacity structuring secure, flexible cloud solutions.", "Paid", "https://aws.amazon.com/certification/certified-solutions-architect-associate/"),
        ResourceItem("AWS DevOps Engineer Professional", "Cloud Computing", "Certifications", "Advanced", "Amazon AWS", "Elite-tier deployment automation, auto-scaling, and failovers.", "Paid", "https://aws.amazon.com/certification/certified-solutions-architect-professional/"),
        ResourceItem("Cloud Academy Playgrounds", "Cloud Computing", "Practice Platforms", "N/A", "Academy Team", "Spin up live sandbox networks with active task checklists.", "Paid", "https://cloudacademy.com/"),
        ResourceItem("Qwiklabs GCP Scenarios", "Cloud Computing", "Practice Platforms", "N/A", "Google / Qwiklabs", "Real cloud console workflows measuring metric success benchmarks.", "Free/Paid", "https://www.qwiklabs.com/"),
        ResourceItem("Serverless Website Portfolio", "Cloud Computing", "Projects", "N/A", "S3 & CloudFront", "Deploy secure resumes with zero server overhead using global CDNs.", "Free", "https://github.com/aws-samples/aws-serverless-website-hosting"),
        ResourceItem("Multi-tier Infra IAC", "Cloud Computing", "Projects", "N/A", "Terraform VPC", "Write declarative infrastructure coding managing complex VPC nodes.", "Free", "https://github.com/terraform-aws-modules/terraform-aws-vpc"),

        // ==========================================
        // 5. UI/UX DESIGN
        // ==========================================
        ResourceItem("Nielsen Norman Group", "UI/UX Design", "YouTube Channels", "N/A", "NNgroup", "Scientific user-testing outcomes and mobile heuristic reviews.", "Free", "https://www.youtube.com/@NNgroup"),
        ResourceItem("Mizko", "UI/UX Design", "YouTube Channels", "N/A", "Mizko", "Exceptional visual tips covering typography scales and Figma styles.", "Free", "https://www.youtube.com/@Mizko"),
        ResourceItem("Uxcel Foundations", "UI/UX Design", "Free Courses", "N/A", "Uxcel", "Bite-sized visual modules measuring dynamic UX/UI principles.", "Free/Paid", "https://uxcel.com/"),
        ResourceItem("Hackdesign Weekly", "UI/UX Design", "Free Courses", "N/A", "Hackdesign", "Weekly layout studies selected by leading software specialists.", "Free", "https://hackdesign.org/"),
        ResourceItem("Google UX Professional", "UI/UX Design", "Paid Courses", "N/A", "Google / Coursera", "The ultimate entry certificate launching dynamic app design drafts.", "Paid", "https://www.coursera.org/professional-certificates/google-ux-design"),
        ResourceItem("Interaction Design Foundation", "UI/UX Design", "Paid Courses", "N/A", "IxDF", "Elite guidelines touching user psychological grids and inputs.", "Paid", "https://www.interaction-design.org/"),
        ResourceItem("Google UX Badge", "UI/UX Design", "Certifications", "Beginner", "Google Team", "Validates wireframing, high-fide prototypes, and asset export.", "Paid", "https://grow.google/certificates/ux-design/"),
        ResourceItem("Nielsen Norman UX Certificate", "UI/UX Design", "Certifications", "Intermediate", "NNgroup", "Requires passing rigorous user research and visual testing exams.", "Paid", "https://www.nngroup.com/ux-certification/"),
        ResourceItem("IxDF Master Certificate", "UI/UX Design", "Certifications", "Advanced", "IxDF Specialist", "Elite design strategy validation for enterprise-level platforms.", "Paid", "https://www.interaction-design.org/get-certified"),
        ResourceItem("Figma Community Files", "UI/UX Design", "Practice Platforms", "N/A", "Figma", "Study dynamic visual systems, layout grids, and mobile UI kits.", "Free", "https://www.figma.com/community"),
        ResourceItem("FinTech Mobile Interface", "UI/UX Design", "Projects", "N/A", "Figma Design", "Assemble color-balanced finance dashboards explaining clear flows.", "Free", "https://www.figma.com/design/"),
        ResourceItem("Charity Landing Web Page", "UI/UX Design", "Projects", "N/A", "Responsive Layout", "Conduct user testing to produce fully optimized mobile mockups.", "Free", "https://github.com/topics/ux-design-portfolio"),

        // ==========================================
        // 6. VIDEO EDITING
        // ==========================================
        ResourceItem("Film Riot Editing", "Video Editing", "YouTube Channels", "N/A", "Film Riot Team", "Master cinematic sound design, action pacing, and asset effects.", "Free", "https://www.youtube.com/@filmriot"),
        ResourceItem("Cinecom Media", "Video Editing", "YouTube Channels", "N/A", "Cinecom Group", "Learn dynamic editing styles, transition setups, and asset guides.", "Free", "https://www.youtube.com/@CinecomMedia"),
        ResourceItem("Resolve Official Lessons", "Video Editing", "Free Courses", "N/A", "Blackmagic Design", "Exemplary video courses covering Resolve's color, edit, and audio nodes.", "Free", "https://www.blackmagicdesign.com/products/davinciresolve/training"),
        ResourceItem("Adobe Starter Portal", "Video Editing", "Free Courses", "N/A", "Adobe Systems", "Fast tutorials explaining timelines, text additions, and tracks.", "Free", "https://helpx.adobe.com/premiere-pro/tutorials.html"),
        ResourceItem("Inside the Edit Narratives", "Video Editing", "Paid Courses", "N/A", "Inside the Edit", "The premium structural edit class focusing strictly on timing.", "Paid", "https://www.insidetheedit.com/"),
        ResourceItem("MZed Hollywood Craft", "Video Editing", "Paid Courses", "N/A", "MZed Academy", "Professional filmmaking, lighting, and advanced master tracks.", "Paid", "https://www.mzed.com/"),
        ResourceItem("DaVinci End-User", "Video Editing", "Certifications", "Beginner", "Blackmagic Design", "Official test confirming timeline control, backing, and export.", "Free", "https://www.blackmagicdesign.com/products/davinciresolve/training"),
        ResourceItem("Adobe Premium Professional", "Video Editing", "Certifications", "Intermediate", "Adobe Team", "Validates premiere mastery, color spaces, and core parameters.", "Paid", "https://certiport.pearsonvue.com/Certifications/Adobe/ACP/Overview"),
        ResourceItem("Avid Media Support Expert", "Video Editing", "Certifications", "Advanced", "Avid Systems", "Ultimate cinema certificate validating enterprise media architectures.", "Paid", "https://www.avid.com/services/education-and-certification"),
        ResourceItem("EditStock Raw Footage", "Video Editing", "Practice Platforms", "N/A", "EditStock", "Download and sync professional raw multi-cam drafts to practice.", "Paid", "https://editstock.com/"),
        ResourceItem("Commercial Visual Spot", "Video Editing", "Projects", "N/A", "Motion Product Spot", "Produce high-converting 30-sec videos with sound styling.", "Free", "https://github.com/topics/video-editing"),
        ResourceItem("YouTube Creative Portfolio", "Video Editing", "Projects", "N/A", "Vlog Edit", "Edit dynamic stories combining multi-cams, graphics, and EQ.", "Free", "https://github.com/topics/video-production"),

        // ==========================================
        // 7. GRAPHIC DESIGN
        // ==========================================
        ResourceItem("The Futur Graphic Design", "Graphic Design", "YouTube Channels", "N/A", "Chris Do", "Learn visual branding theory, client relations, and asset vectors.", "Free", "https://www.youtube.com/@thefutur"),
        ResourceItem("Satori Vector Tutorials", "Graphic Design", "YouTube Channels", "N/A", "Satori Graphics", "Stunning tutorials detail alignment, typographic grids, and branding.", "Free", "https://www.youtube.com/@SatoriGraphics"),
        ResourceItem("Canva Design School Classes", "Graphic Design", "Free Courses", "N/A", "Canva Team", "Brief, clean visual courses explaining palettes and styling loops.", "Free", "https://designschool.canva.com/"),
        ResourceItem("Alison Graphic Design Diploma", "Graphic Design", "Free Courses", "N/A", "Alison Group", "Learn core visual parameters, print formats, and layouts.", "Free", "https://alison.com/course/diploma-in-graphic-design"),
        ResourceItem("CalArts Graphic Special", "Graphic Design", "Paid Courses", "N/A", "CalArts / Coursera", "Rigorous typography, visual structures, and brand asset tracks.", "Paid", "https://www.coursera.org/specializations/graphic-design"),
        ResourceItem("Skillshare Grid Typography", "Graphic Design", "Paid Courses", "N/A", "Skillshare", "Detailed courses focusing on text geometry and scale systems.", "Paid", "https://www.skillshare.com/"),
        ResourceItem("Adobe Illustrator Associate", "Graphic Design", "Certifications", "Beginner", "Adobe Systems", "Certifies structural anchor usage, custom vectors, and paths.", "Paid", "https://certiport.pearsonvue.com/Certifications/Adobe/ACP/Overview"),
        ResourceItem("Adobe Photoshop Professional", "Graphic Design", "Certifications", "Intermediate", "Adobe Systems", "Proves capacity manipulating layers, non-destructive masks, and lights.", "Paid", "https://certiport.pearsonvue.com/Certifications/Adobe/ACP/Overview"),
        ResourceItem("Shillington Design Diploma", "Graphic Design", "Certifications", "Advanced", "Shillington Academy", "Top international credential validating clean product systems.", "Paid", "https://www.shillingtoneducation.com/"),
        ResourceItem("Behance Asset Showcase", "Graphic Design", "Practice Platforms", "N/A", "Adobe Behance", "Examine and review trending layout profiles from master designers.", "Free", "https://www.behance.net/"),
        ResourceItem("Brand Visual Brandbook", "Graphic Design", "Projects", "N/A", "Identity System", "Construct palettes, vector logo sets, and complete guidance docs.", "Free", "https://github.com/topics/brand-identity"),
        ResourceItem("High-Contrast Vector Poster", "Graphic Design", "Projects", "N/A", "Typographic Poster", "Utilize massive negative space, scales, and text layouts beautifully.", "Free", "https://github.com/topics/graphic-design"),

        // ==========================================
        // 8. AI/ML
        // ==========================================
        ResourceItem("Yannic Kilcher Deep-Dive", "AI/ML", "YouTube Channels", "N/A", "Yannic Kilcher", "Dissects complex AI publications, networks, and datasets.", "Free", "https://www.youtube.com/@YannicKilcher"),
        ResourceItem("Sentdex Python Networks", "AI/ML", "YouTube Channels", "N/A", "Harrison Kinsley", "Code raw forward feeds and loss loops directly inside Python.", "Free", "https://www.youtube.com/@sentdex"),
        ResourceItem("Machine Learning (Andrew Ng)", "AI/ML", "Free Courses", "N/A", "DeepLearning.AI", "The legendary, absolute standard course teaching classic regressors.", "Free", "https://www.coursera.org/specializations/machine-learning-introduction"),
        ResourceItem("Fast.ai Deep Learning Lab", "AI/ML", "Free Courses", "N/A", "Fast.ai Team", "Top-down modeling tracks using actual PyTorch pipelines instantly.", "Free", "https://www.fast.ai/"),
        ResourceItem("Coursera Deep Learning Track", "AI/ML", "Paid Courses", "N/A", "Ng Academy", "Structure CNNs, RNNs, transformers, and hyperparameter checks.", "Paid", "https://www.coursera.org/specializations/deep-learning"),
        ResourceItem("Udacity AI Product Specialist", "AI/ML", "Paid Courses", "N/A", "Udacity", "Learn model optimization, testing margins, and metrics.", "Paid", "https://www.udacity.com/course/ai-product-manager-nanodegree--nd089"),
        ResourceItem("TensorFlow Developer", "AI/ML", "Certifications", "Beginner", "Google / TensorFlow", "Validates computer vision and NLP model building with TF.", "Paid", "https://www.tensorflow.org/certificate"),
        ResourceItem("Google Professional ML Engineer", "AI/ML", "Certifications", "Intermediate", "Google Cloud", "Tests capacity orchestrating production ML pipelines on Vertex AI.", "Paid", "https://cloud.google.com/learn/certification/machine-learning-engineer"),
        ResourceItem("AWS ML Specialty Badge", "AI/ML", "Certifications", "Advanced", "Amazon AWS", "Assess complex multi-node cloud database and SageMaker setups.", "Paid", "https://aws.amazon.com/certification/certified-machine-learning-specialty/"),
        ResourceItem("Hugging Face Spaces", "AI/ML", "Practice Platforms", "N/A", "Hugging Face", "Test pre-trained transformer pipelines and download raw assets.", "Free", "https://huggingface.com/"),
        ResourceItem("Custom YOLO Scanner", "AI/ML", "Projects", "N/A", "PyTorch YOLO", "Annotate and deploy custom vision detectors detecting items.", "Free", "https://github.com/topics/object-detection"),
        ResourceItem("Sentiment Analyser API", "AI/ML", "Projects", "N/A", "BERT Transformer", "Deploy custom web pipelines loading fine-tuned sentiment models.", "Free", "https://github.com/topics/sentiment-analysis"),

        // ==========================================
        // 9. DIGITAL MARKETING
        // ==========================================
        ResourceItem("Wes McDowell", "Digital Marketing", "YouTube Channels", "N/A", "Wes McDowell", "Learn funnel conversion patterns and clear landing layouts.", "Free", "https://www.youtube.com/@wesmcdowell"),
        ResourceItem("Neil Patel Marketing", "Digital Marketing", "YouTube Channels", "N/A", "Neil Patel", "Get detailed organic search SEO advice and organic backlinks.", "Free", "https://www.youtube.com/@neilpatel"),
        ResourceItem("Google Digital Garage SEO", "Digital Marketing", "Free Courses", "N/A", "Google", "Vast accredited course detailing dynamic traffic analytics.", "Free", "https://smarter.withgoogle.com/link/fundamentals-digital-marketing"),
        ResourceItem("HubSpot Inbound Marketing", "Digital Marketing", "Free Courses", "N/A", "HubSpot", "Excellent tutorials detailing buyer funnels and organic loops.", "Free", "https://academy.hubspot.com/courses/inbound-marketing"),
        ResourceItem("CXL Growth Marketing", "Digital Marketing", "Paid Courses", "N/A", "CXL Institute", "Advanced analysis course checking conversion optimization and statistics.", "Paid", "https://cxl.com/institute/programs/growth-marketing-minidegree/"),
        ResourceItem("Udacity Lead Marketer", "Digital Marketing", "Paid Courses", "N/A", "Udacity", "Configure and deploy real Facebook ads tracking organic feedback.", "Paid", "https://www.udacity.com/course/digital-marketing-nanodegree--nd018"),
        ResourceItem("GA4 Analytics Qualification", "Digital Marketing", "Certifications", "Beginner", "Google", "Validates tracking visual traffic flows, goals, and sources.", "Free", "https://skillshop.exceedlms.com/student/path/66497-google-analytics-individual-qualification"),
        ResourceItem("Google Search Specialist Badge", "Digital Marketing", "Certifications", "Intermediate", "Google", "Proves competency with keyword bidding and budgeting ads.", "Free", "https://skillshop.exceedlms.com/student/path/18128-google-ads-search-certification"),
        ResourceItem("Meta Digital Marketer Associate", "Digital Marketing", "Certifications", "Advanced", "Meta Team", "Proves advanced multi-channel strategy and enterprise campaign layouts.", "Paid", "https://www.facebook.com/business/learn/certification"),
        ResourceItem("Google Search Analyst Portal", "Digital Marketing", "Practice Platforms", "N/A", "Google Console", "Study real-time keywords, CTR ratios, and search changes.", "Free", "https://search.google.com/search-console/about"),
        ResourceItem("Organic SEO Campaign", "Digital Marketing", "Projects", "N/A", "SEO Ranking Audit", "Construct extensive metadata modifications measuring CTR ranks.", "Free", "https://github.com/topics/seo-audit"),
        ResourceItem("PPC Dynamic Ad Layout", "Digital Marketing", "Projects", "N/A", "Ad Split-testing", "Structure structured split testing maps using high-converting copies.", "Free", "https://github.com/topics/google-ads"),

        // ==========================================
        // 10. PRODUCT MANAGEMENT
        // ==========================================
        ResourceItem("Product School Sessions", "Product Management", "YouTube Channels", "N/A", "Product School", "Excellent talks detailing prioritization, tools, and sprint specs.", "Free", "https://www.youtube.com/@ProductSchool"),
        ResourceItem("Diego Granados PM Info", "Product Management", "YouTube Channels", "N/A", "Diego Granados", "Clear guidance explaining backlog handling and daily rituals.", "Free", "https://www.youtube.com/@PMDiegoGranados"),
        ResourceItem("Product PM 101 Essentials", "Product Management", "Free Courses", "N/A", "Udemy Academy", "High-level overview covering product life schedules and boards.", "Free", "https://www.udemy.com/course/product-management-101/"),
        ResourceItem("Product-Led Growth Classes", "Product Management", "Free Courses", "N/A", "ProductLed", "Explores dynamic modern retention, activation, and metrics.", "Free", "https://productled.com/certification/"),
        ResourceItem("Pragmatic Institute System", "Product Management", "Paid Courses", "N/A", "Pragmatic Group", "Detailed course teaching product market segmentation and prices.", "Paid", "https://www.pragmaticinstitute.com/product/"),
        ResourceItem("Udacity PM Nanodegree", "Product Management", "Paid Courses", "N/A", "Udacity", "Assemble detailed requirement docs and custom UX plans.", "Paid", "https://www.udacity.com/course/product-manager-nanodegree--nd036"),
        ResourceItem("Professional Scrum Owner (PSPO)", "Product Management", "Certifications", "Beginner", "Scrum.org", "Proves scrum knowledge, backlog trimming, and delivery flow.", "Paid", "https://www.scrum.org/professional-scrum-product-owner-i-certification"),
        ResourceItem("AIPMM Certified PM Associate", "Product Management", "Certifications", "Intermediate", "AIPMM Group", "Comprehensive validation covering road mapping and marketing plans.", "Paid", "https://aipmm.com/cpm-certified-product-manager"),
        ResourceItem("Pragmatic Product Master", "Product Management", "Certifications", "Advanced", "Pragmatic Expert", "The gold-standard enterprise qualification validating multi-products.", "Paid", "https://www.pragmaticinstitute.com/product/"),
        ResourceItem("Product Hunt Launch Feed", "Product Management", "Practice Platforms", "N/A", "Product Hunt", "Analyse and evaluate thousands of startup onboarding flows.", "Free", "https://www.producthunt.com/"),
        ResourceItem("Product Requirement Document", "Product Management", "Projects", "N/A", "PRD Specification", "Draft incredibly detailed PRDs defining specs for apps.", "Free", "https://github.com/topics/product-requirement-document"),
        ResourceItem("Product Roadmap Matrix", "Product Management", "Projects", "N/A", "Roadmap Blueprint", "Build priority scores aligning releases timelines beautifully.", "Free", "https://github.com/topics/product-roadmap"),

        // ==========================================
        // 11. APP DEVELOPMENT
        // ==========================================
        ResourceItem("Philipp Lackner Android", "App Development", "YouTube Channels", "N/A", "Philipp Lackner", "The ultimate expert modern Compose, Flow, and clean designs channel.", "Free", "https://www.youtube.com/@PhilippLackner"),
        ResourceItem("CodingWithMitch Mobile", "App Development", "YouTube Channels", "N/A", "Mitch Tabian", "Excellent, deep unit test guides and Room caching paths.", "Free", "https://www.youtube.com/@kanyewest"),
        ResourceItem("Android Basics with Compose", "App Development", "Free Courses", "N/A", "Google Developers", "Official code-heavy path teaching clean Android Kotlin apps.", "Free", "https://developer.android.com/courses/android-basics-compose/course"),
        ResourceItem("SwiftPlaygrounds iOS Path", "App Development", "Free Courses", "N/A", "Apple", "Interactive, stunning course building real SwiftUI targets on iOS.", "Free", "https://www.apple.com/swift/playgrounds/"),
        ResourceItem("React Native Mobile Track", "App Development", "Paid Courses", "N/A", "Stephen Grider", "Assemble robust cross-platform targets using React and Redux.", "Paid", "https://www.udemy.com/user/stephen-grider/"),
        ResourceItem("Kodeco Mobile Subscription", "App Development", "Paid Courses", "N/A", "Kodeco Portal", "Premium, massive collection of Kotlin and Swift tutorials.", "Paid", "https://www.kodeco.com/"),
        ResourceItem("Associate Android Developer", "App Development", "Certifications", "Beginner", "Google", "Verifies SQLite DBs, safe system threads, and layout rules.", "Paid", "https://developer.android.com/credentials/associate-android-developer"),
        ResourceItem("Meta Mobile Professional", "App Development", "Certifications", "Intermediate", "Meta / Coursera", "Proves competency publishing mobile targets to marketplaces.", "Paid", "https://www.coursera.org/professional-certificates/meta-android-developer"),
        ResourceItem("ECC Mobile App Sec (CMAD)", "App Development", "Certifications", "Advanced", "EC-Council", "Validates advanced local security, auth setups, and caches.", "Paid", "https://www.eccouncil.org/train-certify/certified-mobile-application-developer-cmad/"),
        ResourceItem("Android Developers Center", "App Development", "Practice Platforms", "N/A", "Google Developer", "Dissect custom system design files, UI kits, and APIs.", "Free", "https://developer.android.com/"),
        ResourceItem("Offline-first Habit App", "App Development", "Projects", "N/A", "Compose & Room", "Build full-fledged database-driven scheduling mobile apps.", "Free", "https://github.com/topics/habit-tracker"),
        ResourceItem("Real-time REST Weather App", "App Development", "Projects", "N/A", "Weather Api", "Assemble clean apps rendering weather vectors and caching databases.", "Free", "https://github.com/topics/weather-app")
    )

    // ------------------------------------------------------------------------
    // DATABASE ACTIONS
    // ------------------------------------------------------------------------

    // Use shared preferences to cache dynamic content locally
    private val prefs = context.getSharedPreferences("skillstacker_prefs", Context.MODE_PRIVATE)

    // ------------------------------------------------------------------------
    // DATABASE ACTIONS
    // ------------------------------------------------------------------------

    suspend fun saveProfile(
        email: String,
        fullName: String,
        branch: String,
        year: String,
        careerGoal: String,
        currentLevel: String,
        interests: String,
        token: String? = null
    ) {
        // 1. Save locally to SQLite via Room
        val entity = ProfileEntity(
            email = email,
            fullName = fullName,
            branch = branch,
            year = year,
            careerGoal = careerGoal,
            currentLevel = currentLevel,
            interests = interests
        )
        appDao.insertProfile(entity)

        // 2. Clear old cached raw data to ensure reload of fresh dynamic tracks
        prefs.edit()
            .remove("cached_roadmap_json")
            .remove("cached_gap_analysis_json")
            .remove("cached_daily_coach_json")
            .apply()

        // 3. Generate and populate custom beautiful local pre-made roadmap
        createPredefinedRoadmap(careerGoal)

        // 4. Attempt Supabase sync
        try {
            if (NetworkClients.hasSupabaseConfig()) {
                NetworkClients.supabaseUpsertProfile(
                    email, fullName, branch, year, careerGoal, currentLevel, interests, token
                )
            }
        } catch (e: Exception) {
            // Log & ignore (offline-fallback)
        }
    }

    suspend fun clearSessionAndProfile() {
        appDao.clearProfiles()
        appDao.clearRoadmapTasks()
        prefs.edit()
            .remove("cached_roadmap_json")
            .remove("cached_gap_analysis_json")
            .remove("cached_daily_coach_json")
            .apply()
    }

    suspend fun updateTaskStatus(id: Int, isCompleted: Boolean, email: String, token: String?) {
        // 1. Update Room DB
        appDao.updateTaskStatus(id, isCompleted)

        // 2. Grab task info to find inside the JSON to update check states
        val task = appDao.getTaskById(id) ?: return

        // 3. Update JSON cache in Shared Preferences
        val cachedJsonStr = prefs.getString("cached_roadmap_json", null)
        if (!cachedJsonStr.isNullOrEmpty()) {
            try {
                val jsonObject = org.json.JSONObject(cachedJsonStr)
                val phasesArr = jsonObject.optJSONArray("phases")
                if (phasesArr != null) {
                    var modified = false
                    for (i in 0 until phasesArr.length()) {
                        val phaseObj = phasesArr.getJSONObject(i)
                        val tasksArr = phaseObj.optJSONArray("tasks")
                        if (tasksArr != null) {
                            for (j in 0 until tasksArr.length()) {
                                val taskObj = tasksArr.getJSONObject(j)
                                if (taskObj.optString("title") == task.title) {
                                    taskObj.put("isCompleted", isCompleted)
                                    modified = true
                                }
                            }
                        }
                    }
                    if (modified) {
                        val updatedJsonStr = jsonObject.toString()
                        prefs.edit().putString("cached_roadmap_json", updatedJsonStr).apply()

                        // 4. Push updated JSON to Supabase
                        if (NetworkClients.hasSupabaseConfig() && email.isNotEmpty()) {
                            try {
                                NetworkClients.supabaseUpsertRoadmap(email, updatedJsonStr, token)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ------------------------------------------------------------------------
    // ROADMAP GENERATORS & CORE ALGORITHMS
    // ------------------------------------------------------------------------

    private suspend fun createPredefinedRoadmap(careerGoal: String) {
        appDao.clearRoadmapTasks()
        val defaultTasks = getPredefinedTasksForGoal(careerGoal)
        appDao.insertRoadmapTasks(defaultTasks)
    }

    fun getPredefinedTasksForGoal(careerGoal: String): List<RoadmapTaskEntity> {
        return when (careerGoal) {
            "Full Stack Developer" -> listOf(
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Foundations", title = "HTML, CSS, and modern CSS Frameworks", description = "Master flexbox, grid, responsive design, and Tailwind structures."),
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Foundations", title = "JavaScript (ES6+) Core Mechanics", description = "Deep dive into async-await, fetch APIs, promises, array methods, and DOM manipulation."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Frontend & Frameworks", title = "React Framework Mastery", description = "Learn hooks, custom state providers, routing, and component lifecycles."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Frontend & Frameworks", title = "Client-Side Build Optimizations", description = "Understand package bundles, bundlers, and critical rendering paths."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Service Layer & Backends", title = "NodeJS, RESTful APIs, and SQL Data Layer", description = "Create Express routes, build schemas, and connect PostgreSQL or Room database."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Service Layer & Backends", title = "Secure Deployments & Pipelines", description = "Manage cloud deployments, setup CORS, authenticate user credentials securely, and audit security.")
            )
            "Cybersecurity Engineer" -> listOf(
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Networking & Systems", title = "TCP/IP Suite & Network Topologies", description = "Understand Subnetting, routing tables, DNS, and fundamental data transit."),
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Networking & Systems", title = "Linux Command Shell Foundations", description = "Learn system processes, permissions, logs auditing, and shell scripting."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Defensive Operations", title = "Cryptography & Secure Architecture", description = "Implement AES/RSA, verify certificate authority parameters, hashing, and HTTPS protocols."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Defensive Operations", title = "Firewalling & Network Guarding", description = "Configure IDS/IPS, manage packet filtering rules, and analyze Wireshark payloads."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Offensive Auditing", title = "Penetration Testing & Breach Audits", description = "Use Kali tools to perform network mapping, discover code exploits, and document architectural security reports.")
            )
            "Data Scientist" -> listOf(
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Core Mathematical Toolkit", title = "Linear Algebra, Calculus, & Basic Stats", description = "Master probabilities, vector calculus, distributions, and matrix operations."),
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Core Mathematical Toolkit", title = "Data Wrangling with Python", description = "Master Pandas, NumPy, text transformations, and advanced file IO pipelines."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Visualizing & Modeling", title = "Descriptive Data Mining & Visuals", description = "Build rich charts using Matplotlib, Seaborn, and clean up missing values safely."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Visualizing & Modeling", title = "Classical Supervised Algorithms", description = "Implement linear/logistic regressions, decision trees, and evaluate models (ROC, F1)."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Production Pipeline", title = "SQL Storage & Cloud ML Deploy", description = "Query large database systems, write server endpoints block, and ship prediction models to API instances.")
            )
            "UI/UX Designer" -> listOf(
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Visual Design Axioms", title = "Typography, Spacing, and Hierarchies", description = "Understand type scale, golden ratios, layout alignment, and positive/negative spacing."),
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Visual Design Axioms", title = "Figma Vector Precision & Tooling", description = "Master auto-layout templates, components, variants, properties, and micro-layouts."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Research & Prototypes", title = "Heuristic Auditing & Mapping Flows", description = "Perform active user testing, map persona actions, and resolve critical usability blocks."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Research & Prototypes", title = "Micro-Interactions & Prototyping", description = "Build smart animated prototypes modeling complex state interactions elegantly."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Design Specs & Handoff", title = "Design Systems & Token Architecture", description = "Structure design systems, annotate tokens for developer handoff, and inspect CSS layouts.")
            )
            "App Developer" -> listOf(
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Platform Foundations", title = "Kotlin Core Syntax & Coroutines", description = "Learn OOP/FP ideas, properties, flow controls, and asynchronous programming model."),
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Platform Foundations", title = "Jetpack Compose Declarative UI", description = "Build responsive layouts with Scaffold, Rows, Columns, Cards, and Material 3."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: State & Storage", title = "Room Database Local Persistence", description = "Implement local SQL caching, model entities, write query DAOs, and observe states with Flows."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: State & Storage", title = "REST Networking & Retrofit Client", description = "Connect app to external endpoints securely, parse JSON configurations, and handle network states."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Production Polishing", title = "Custom Graphics, Icons & Play Release", description = "Add custom Canvas drawings, design adaptive vector launcher symbols, optimize Gradle variables, and upload AAB bundle.")
            )
            else -> listOf(
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Getting Started", title = "Core Terminology & Essential Tools", description = "Familiarize yourself with the fundamental workflows and tools used by top industry professionals."),
                RoadmapTaskEntity(phase = 1, phaseTitle = "Phase 1: Getting Started", title = "Case Study Analysis", description = "Examine industry-leading examples of work and reverse-engineer their success patterns."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Deep Skill Drills", title = "Hands-on Guided Experiments", description = "Build smaller focused exercises designed to test and consolidate intermediate capabilities."),
                RoadmapTaskEntity(phase = 2, phaseTitle = "Phase 2: Deep Skill Drills", title = "Portfolio Piece Creation", description = "Plan and design a professional-grade project showcasing your individual voice and technique."),
                RoadmapTaskEntity(phase = 3, phaseTitle = "Phase 3: Mastering Commercial Handoff", title = "Business Operations & Professional Brand", description = "Setup a clean visual portfolio website, frame your services, and audit growth metrics.")
            )
        }
    }

    // ------------------------------------------------------------------------
    // SUPABASE HYBRID DATA SYNCHRONIZATION (Load Existing Data)
    // ------------------------------------------------------------------------

    suspend fun fetchAndSyncSupabaseData(email: String, token: String?) {
        try {
            val roadmapJson = NetworkClients.supabaseFetchRoadmap(email, token)
            if (!roadmapJson.isNullOrEmpty()) {
                prefs.edit().putString("cached_roadmap_json", roadmapJson).apply()
                val cleaned = roadmapJson.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                val jsonObject = JSONObject(cleaned)
                val phasesArr = jsonObject.optJSONArray("phases")
                if (phasesArr != null) {
                    val generatedTasks = ArrayList<RoadmapTaskEntity>()
                    for (i in 0 until phasesArr.length()) {
                        val phaseObj = phasesArr.getJSONObject(i)
                        val phaseNum = phaseObj.optInt("phase", i + 1)
                        val phaseTitle = phaseObj.optString("phaseTitle", "Phase $phaseNum")
                        val tasksArr = phaseObj.optJSONArray("tasks")
                        if (tasksArr != null) {
                            for (j in 0 until tasksArr.length()) {
                                val taskObj = tasksArr.getJSONObject(j)
                                generatedTasks.add(
                                    RoadmapTaskEntity(
                                        phase = phaseNum,
                                        phaseTitle = phaseTitle,
                                        title = taskObj.optString("title", ""),
                                        description = taskObj.optString("description", ""),
                                        isAiGenerated = true
                                    )
                                )
                            }
                        }
                    }
                    if (generatedTasks.isNotEmpty()) {
                        appDao.clearRoadmapTasks()
                        appDao.insertRoadmapTasks(generatedTasks)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val analysisJson = NetworkClients.supabaseFetchSkillAnalysis(email, token)
            if (!analysisJson.isNullOrEmpty()) {
                prefs.edit().putString("cached_gap_analysis_json", analysisJson).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ------------------------------------------------------------------------
    // FEATURE 1: AI ROADMAP GENERATOR
    // ------------------------------------------------------------------------

    suspend fun generateAiRoadmap(
        email: String,
        careerGoal: String,
        currentLevel: String,
        interests: String,
        token: String?
    ): Boolean {
        if (!NetworkClients.hasGeminiConfig()) {
            throw Exception("Gemini key not configured. Please add GEMINI_API_KEY.")
        }

        val prompt = """
            You are SkillStacker AI Career Consultant.
            Generate a personalized professional learning roadmap in strict JSON format for a student targeting: '$careerGoal'.
            Their current level is: '$currentLevel'.
            Their stated interests are: '$interests'.
            
            Return a direct JSON object (DO NOT use backticks, markdown, or any explanation text) with these properties:
            - "learningTimeline": string indicating complete time duration (e.g., "16 Weeks")
            - "phases": array of 3 objects, each representing a learning milestone/phase with properties:
              - "phase": integer (1, 2, or 3)
              - "phaseTitle": string showing phase overview (e.g., "Phase 1: Foundations")
              - "skillsToLearn": array of 3 core skill strings
              - "recommendedResources": array of 2 free high-quality online course links/names
              - "beginnerProjects": array of 2 project ideas
              - "weeklyMilestones": array of 2 weekly milestones
              - "certifications": array of 2 free/paid key industry certifications
              - "tasks": array of 3 checkable milestone tasks, each containing action properties:
                - "title": string (the focus skill name, tools, or libraries)
                - "description": string (one sentence summarizing what to study or set up)

            Return ONLY the valid raw JSON object. Do not wrap the output in ```json or ``` blocks.
        """.trimIndent()

        return try {
            val rawResponse = NetworkClients.generateGeminiResponse(prompt)
            if (rawResponse.isNullOrEmpty()) throw Exception("Unable to generate roadmap. Please try again.")

            val cleaned = rawResponse.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val jsonObject = JSONObject(cleaned)
            val phasesArr = jsonObject.getJSONArray("phases")
            val generatedTasks = ArrayList<RoadmapTaskEntity>()

            for (i in 0 until phasesArr.length()) {
                val phaseObj = phasesArr.getJSONObject(i)
                val phaseNum = phaseObj.optInt("phase", i + 1)
                val phaseTitle = phaseObj.optString("phaseTitle", "Phase $phaseNum")
                val tasksArr = phaseObj.getJSONArray("tasks")
                for (j in 0 until tasksArr.length()) {
                    val taskObj = tasksArr.getJSONObject(j)
                    generatedTasks.add(
                        RoadmapTaskEntity(
                            phase = phaseNum,
                            phaseTitle = phaseTitle,
                            title = taskObj.optString("title", ""),
                            description = taskObj.optString("description", ""),
                            isAiGenerated = true
                        )
                    )
                }
            }

            if (generatedTasks.isNotEmpty()) {
                // Save locally to database
                appDao.clearRoadmapTasks()
                appDao.insertRoadmapTasks(generatedTasks)

                // Cache raw response locally
                prefs.edit().putString("cached_roadmap_json", cleaned).apply()

                // Save to Supabase
                try {
                    NetworkClients.supabaseUpsertRoadmap(email, cleaned, token)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return true
            }
            throw Exception("Unable to generate roadmap. Please try again.")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun getCachedRoadmapJson(): String? {
        return prefs.getString("cached_roadmap_json", null)
    }

    // ------------------------------------------------------------------------
    // FEATURE 2: AI SKILL GAP ANALYZER
    // ------------------------------------------------------------------------

    suspend fun analyzeSkillGap(
        email: String,
        careerGoal: String,
        currentSkills: String,
        token: String?
    ): SkillGapReport {
        if (!NetworkClients.hasGeminiConfig()) {
            return getLocalSkillGapReport(careerGoal, currentSkills)
        }

        val prompt = """
            You are SkillStacker AI Talent Analytics.
            Perform a professional skill gap analysis for a student targeting: '$careerGoal'.
            The skills they currently know or are interested in are: '$currentSkills'.

            Return a direct JSON object (DO NOT use backticks, markdown, or any explanation) with these properties:
            - "readinessScore": integer between 10 and 100 representing market readiness.
            - "missingSkills": array of 3 strings outlining high-demand missing skills.
            - "recommendations": array of 3 strings with tactical actions.
            - "recommendedSkills": array of 3 hot skills they should pick up.
            - "recommendedProjects": array of 3 specific portfolio-grade project ideas.
            - "estimatedTime": string (e.g. "6 Months" or "1.5 Years") of estimated study time to become job-ready.
            - "readinessSummary": string outlining current industrial demand and requirements.

            Return ONLY the valid raw JSON object. Do not wrap in ```json or ``` brackets.
        """.trimIndent()

        return try {
            val rawResponse = NetworkClients.generateGeminiResponse(prompt)
            if (rawResponse.isNullOrEmpty()) return getLocalSkillGapReport(careerGoal, currentSkills)

            val cleaned = rawResponse.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleaned)
            val missing = ArrayList<String>()
            val missingArr = json.getJSONArray("missingSkills")
            for (i in 0 until missingArr.length()) {
                missing.add(missingArr.getString(i))
            }

            val recs = ArrayList<String>()
            val recsArr = json.getJSONArray("recommendations")
            for (i in 0 until recsArr.length()) {
                recs.add(recsArr.getString(i))
            }

            val skills = ArrayList<String>()
            val skillsArr = json.optJSONArray("recommendedSkills")
            if (skillsArr != null) {
                for (i in 0 until skillsArr.length()) {
                    skills.add(skillsArr.getString(i))
                }
            } else {
                skills.addAll(listOf("Communication", "Version Control", "Problem Solving"))
            }

            val projects = ArrayList<String>()
            val projectsArr = json.optJSONArray("recommendedProjects")
            if (projectsArr != null) {
                for (i in 0 until projectsArr.length()) {
                    projects.add(projectsArr.getString(i))
                }
            } else {
                projects.addAll(listOf("Automated Unit Test Automation Framework", "Real-time Dashboard Analytics Center", "Cloud VPS Web Server Deployment"))
            }

            // Cache response on SharedPreferences
            prefs.edit().putString("cached_gap_analysis_json", cleaned).apply()

            // Save to Supabase DB table
            try {
                NetworkClients.supabaseUpsertSkillAnalysis(email, currentSkills, careerGoal, cleaned, token)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            SkillGapReport(
                readinessScore = json.optInt("readinessScore", 45),
                missingSkills = missing,
                recommendations = recs,
                recommendedSkills = skills,
                recommendedProjects = projects,
                estimatedTime = json.optString("estimatedTime", "6 Months"),
                readinessSummary = json.optString("readinessSummary", "Your background aligns with foundational entry-level benchmarks."),
                isRealAi = true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            getLocalSkillGapReport(careerGoal, currentSkills)
        }
    }

    fun getCachedSkillGapReport(): SkillGapReport? {
        val cached = prefs.getString("cached_gap_analysis_json", null) ?: return null
        return try {
            val json = JSONObject(cached)
            val missing = ArrayList<String>()
            val missingArr = json.getJSONArray("missingSkills")
            for (i in 0 until missingArr.length()) missing.add(missingArr.getString(i))

            val recs = ArrayList<String>()
            val recsArr = json.getJSONArray("recommendations")
            for (i in 0 until recsArr.length()) recs.add(recsArr.getString(i))

            val skills = ArrayList<String>()
            val skillsArr = json.optJSONArray("recommendedSkills")
            if (skillsArr != null) {
                for (i in 0 until skillsArr.length()) skills.add(skillsArr.getString(i))
            } else {
                skills.addAll(listOf("Communication", "Version Control", "Problem Solving"))
            }

            val projects = ArrayList<String>()
            val projectsArr = json.optJSONArray("recommendedProjects")
            if (projectsArr != null) {
                for (i in 0 until projectsArr.length()) projects.add(projectsArr.getString(i))
            } else {
                projects.addAll(listOf("Automated Unit Test Automation Framework", "Real-time Dashboard Analytics Center", "Cloud VPS Web Server Deployment"))
            }

            SkillGapReport(
                readinessScore = json.optInt("readinessScore", 45),
                missingSkills = missing,
                recommendations = recs,
                recommendedSkills = skills,
                recommendedProjects = projects,
                estimatedTime = json.optString("estimatedTime", "6 Months"),
                readinessSummary = json.optString("readinessSummary", "Loaded from local cloud-synchronized analysis records."),
                isRealAi = true
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getLocalSkillGapReport(careerGoal: String, currentSkills: String): SkillGapReport {
        val parsedInterests = currentSkills.lowercase()
        val hasProgramming = parsedInterests.contains("web") || parsedInterests.contains("science") || parsedInterests.contains("developer")
        val score = if (hasProgramming) 55 else 35

        val missing = when (careerGoal) {
            "Full Stack Developer" -> listOf("Enterprise NestJS, GraphQL, and serverless queries", "Industrial CI/CD & GitHub Actions pipelines", "Dockerization, Kubernetes, and VPC management")
            "Cybersecurity Engineer" -> listOf("Intrusion Prevention configurations (Snort/Suricata)", "SIEM orchestration & ELK stack dashboards", "Deep Assembly reverse engineering")
            "Data Scientist" -> listOf("PySpark & distributed cluster databases", "Production ML pipelines (Kubeflow/MLflow)", "Deep PyTorch Convolutional neural networks")
            "UI/UX Designer" -> listOf("Advanced Design Token workflows (Style Dictionary)", "Detailed accessibility design audits", "Automated usability telemetry setups")
            "App Developer" -> listOf("Kotlin Symbol Processors & Multiplatform", "Custom Android Audio synthesis systems", "Advanced Play Asset Deliveries")
            else -> listOf("Professional corporate brand identity handoffs", "Advanced metrics tracking & SEO tag campaigns", "Inbound sales client prospecting channels")
        }

        val recs = when (careerGoal) {
            "Full Stack Developer" -> listOf(
                "Build a complex local fullstack project using SQLite Room and Retrofit.",
                "Solve 2 advanced algorithm exercises daily on LeetCode.",
                "Enroll in the free FullStackOpen curriculum."
            )
            "Cybersecurity Engineer" -> listOf(
                "Set up a local isolated virtual sandboxed server environment.",
                "Begin checking off Kali Linux penetration labs.",
                "Review the official CompTIA Security+ materials."
            )
            else -> listOf(
                "Complete one high-fidelity portfolio entry weekly.",
                "Join an active community of creators and get reviews.",
                "Build templates to streamline your startup execution speed."
            )
        }

        return SkillGapReport(
            readinessScore = score,
            missingSkills = missing,
            recommendations = recs,
            recommendedSkills = listOf("Git & GitHub collaboration", "M3 Design Axioms", "Logical problem solving"),
            recommendedProjects = listOf("Local Sandbox SQLite storage explorer", "Custom UI style kit project"),
            estimatedTime = "6 Months",
            readinessSummary = "Simulated benchmark based on selected professional-level parameters.",
            isRealAi = false
        )
    }

    // ------------------------------------------------------------------------
    // FEATURE 3: AI DAILY COACH
    // ------------------------------------------------------------------------

    suspend fun generateDailyRecommendation(
        careerGoal: String,
        progress: String,
        currentSkills: String
    ): DailyRecommendation? {
        if (!NetworkClients.hasGeminiConfig()) return null

        val prompt = """
            You are SkillStacker Daily Learning Coach.
            Analyze this student context to generate dynamic today's learning recommendations:
            - Career Goal: '$careerGoal'
            - Learning Progress: '$progress'
            - Declared Skills/Interests: '$currentSkills'

            Return a direct JSON object (DO NOT use backticks, markdown, or any explanation) with these properties:
            - "todaysFocus": string outlining a very concrete, single concept to read or write today.
            - "reason": string explaining exactly why this topic is the absolute logical next step in their career.
            - "estimatedDuration": string (e.g., "45 mins" or "1.5 hours")
            - "recommendedResource": string detailing a solid, high-quality free online tutorial resource name.

            Return ONLY raw JSON. Do not include ```json or other formatting.
        """.trimIndent()

        return try {
            val rawResponse = NetworkClients.generateGeminiResponse(prompt) ?: return null
            val cleaned = rawResponse.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleaned)
            val rec = DailyRecommendation(
                todaysFocus = json.getString("todaysFocus"),
                reason = json.getString("reason"),
                estimatedDuration = json.getString("estimatedDuration"),
                recommendedResource = json.getString("recommendedResource")
            )

            // Cache
            prefs.edit().putString("cached_daily_coach_json", cleaned).apply()
            rec
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getCachedDailyRecommendation(): DailyRecommendation? {
        val cached = prefs.getString("cached_daily_coach_json", null) ?: return null
        return try {
            val json = JSONObject(cached)
            DailyRecommendation(
                todaysFocus = json.getString("todaysFocus"),
                reason = json.getString("reason"),
                estimatedDuration = json.getString("estimatedDuration"),
                recommendedResource = json.getString("recommendedResource")
            )
        } catch (e: Exception) {
            null
        }
    }

    // ------------------------------------------------------------------------
    // FEATURE 4: AI CAREER MATCH ("I don't know yet" helper)
    // ------------------------------------------------------------------------

    suspend fun generateCareerMatches(
        branch: String,
        year: String,
        interests: String
    ): List<CareerMatch>? {
        if (!NetworkClients.hasGeminiConfig()) return null

        val prompt = """
            You are SkillStacker AI Career Consultant.
            Perform analytical mapping to suggest top 5 optimal career tracks based on:
            - College branch: '$branch'
            - Academic class: '$year year'
            - Extracurricular skill interests: '$interests'

            Return a direct JSON array (DO NOT use markdown, backticks, or any conversational responses) with exactly 5 objects.
            Each object MUST possess these exact properties:
            - "career": string name of career (e.g. "Full Stack Developer", "Cybersecurity Engineer")
            - "matchPercentage": integer representing probability percentage (e.g. 92)
            - "reasoning": string summarizing why it aligns
            - "startingSkills": array of 3 initial skill strings to learn
            - "roadmapSummary": string defining summary of study path

            Return ONLY the valid raw JSON array. Do not use markdown wrappers.
        """.trimIndent()

        return try {
            val rawResponse = NetworkClients.generateGeminiResponse(prompt) ?: return null
            val cleaned = rawResponse.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val array = JSONArray(cleaned)
            val matches = ArrayList<CareerMatch>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val skills = ArrayList<String>()
                val skillsArr = obj.getJSONArray("startingSkills")
                for (j in 0 until skillsArr.length()) {
                    skills.add(skillsArr.getString(j))
                }

                matches.add(
                    CareerMatch(
                        career = obj.getString("career"),
                        matchPercentage = obj.optInt("matchPercentage", 80),
                        reasoning = obj.getString("reasoning"),
                        startingSkills = skills,
                        roadmapSummary = obj.getString("roadmapSummary")
                    )
                )
            }
            matches
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// ------------------------------------------------------------------------
// DATA MODELS REPRESENTING SPECIALIZED AI STRUCTURES
// ------------------------------------------------------------------------

data class SkillGapReport(
    val readinessScore: Int,
    val missingSkills: List<String>,
    val recommendations: List<String>,
    val recommendedSkills: List<String> = emptyList(),
    val recommendedProjects: List<String> = emptyList(),
    val estimatedTime: String = "6 Months",
    val readinessSummary: String = "Not synchronized with backend AI benchmarks.",
    val isRealAi: Boolean = false
)

data class DailyRecommendation(
    val todaysFocus: String,
    val reason: String,
    val estimatedDuration: String,
    val recommendedResource: String
)

data class CareerMatch(
    val career: String,
    val matchPercentage: Int,
    val reasoning: String,
    val startingSkills: List<String>,
    val roadmapSummary: String
)
